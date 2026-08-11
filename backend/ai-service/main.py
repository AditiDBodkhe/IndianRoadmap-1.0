from __future__ import annotations

import os
from typing import Any, Literal

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

DESTINATION_BASE_URL = os.getenv("DESTINATION_SERVICE_URL", "http://localhost:8081")
ROADMAP_BASE_URL = os.getenv("ROADMAP_SERVICE_URL", "http://localhost:8082")
STORY_BASE_URL = os.getenv("STORY_SERVICE_URL", "http://localhost:8083")
RECOMMENDATION_BASE_URL = os.getenv("RECOMMENDATION_SERVICE_URL", "http://localhost:8085")
PORT = int(os.getenv("PORT", "8090"))

app = FastAPI(
    title="IndianRoadmap AI Service",
    version="0.0.1",
    description="Heuristic recommendation and semantic search service backed by live IndianRoadmap APIs.",
)


class MoodRecommendationRequest(BaseModel):
    mood: str = Field(min_length=2)
    durationDays: int | None = Field(default=None, ge=1, le=21)
    budget: Literal["BUDGET", "MID_RANGE", "PREMIUM"] | None = None


class DestinationRecommendationRequest(BaseModel):
    destinationId: str = Field(min_length=2)
    preferences: list[str] = Field(default_factory=list)


class RoadmapRecommendationRequest(BaseModel):
    mood: str | None = None
    region: str | None = None
    durationDays: int | None = Field(default=None, ge=1, le=30)


class SemanticSearchRequest(BaseModel):
    query: str = Field(min_length=3)
    limit: int = Field(default=10, ge=1, le=50)


def map_mood(input_mood: str) -> str:
    normalized = input_mood.strip().upper().replace("-", "_").replace(" ", "_")
    lookup = {
        "PEACE": "ZEN",
        "SPIRITUALITY": "SPIRITUAL",
        "ADVENTURE": "ADVENTUROUS",
        "HISTORY": "CULTURAL",
        "NATURE": "OFFBEAT",
        "FOOD": "CULTURAL",
        "CULTURE": "CULTURAL",
        "ROMANCE": "ROMANTIC",
        "WILDLIFE": "OFFBEAT",
        "PHOTOGRAPHY": "CURIOUS",
        "ROAD_TRIP": "ADVENTUROUS",
        "HIDDEN_GEMS": "OFFBEAT",
    }
    return lookup.get(normalized, normalized if normalized in {
        "ZEN", "ADVENTUROUS", "SPIRITUAL", "CURIOUS", "ROMANTIC", "CULTURAL", "OFFBEAT", "SOCIAL", "SOLITUDE", "FAMILY"
    } else "CURIOUS")


def max_budget(budget: str | None) -> int | None:
    if budget == "BUDGET":
        return 25000
    if budget == "MID_RANGE":
        return 70000
    if budget == "PREMIUM":
        return 150000
    return None


def destination_name(value: Any) -> str:
    if isinstance(value, dict):
        return str(value.get("defaultName") or value.get("primary") or value.get("english") or "Unknown destination")
    if isinstance(value, str):
        return value
    return "Unknown destination"


def safe_json(response: httpx.Response) -> dict[str, Any]:
    try:
        return response.json()
    except Exception as ex:  # noqa: BLE001
        raise HTTPException(status_code=502, detail=f"Invalid upstream response: {ex}") from ex


async def get_json(client: httpx.AsyncClient, url: str, params: dict[str, Any] | None = None) -> dict[str, Any]:
    response = await client.get(url, params=params)
    if response.status_code == 404:
        return {"success": True, "data": []}
    if response.status_code >= 400:
        raise HTTPException(status_code=502, detail=f"Upstream GET failed: {url} ({response.status_code})")
    return safe_json(response)


async def post_json(client: httpx.AsyncClient, url: str, body: dict[str, Any]) -> dict[str, Any]:
    response = await client.post(url, json=body)
    if response.status_code >= 400:
        raise HTTPException(status_code=502, detail=f"Upstream POST failed: {url} ({response.status_code})")
    return safe_json(response)


def normalized_score(value: Any) -> float:
    try:
        score = float(value)
    except (TypeError, ValueError):
        return 0.0
    if score > 1.0:
        score = score / 100.0
    return round(max(0.0, min(1.0, score)), 3)


@app.get("/actuator/health")
async def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/api/ai/recommendations/mood")
async def mood_recommendations(request: MoodRecommendationRequest) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "mood": map_mood(request.mood),
        "durationDays": request.durationDays,
        "maxBudget": max_budget(request.budget),
        "limit": 12,
    }
    payload = {k: v for k, v in payload.items() if v is not None}

    async with httpx.AsyncClient(timeout=12.0) as client:
        rec = await post_json(client, f"{RECOMMENDATION_BASE_URL}/api/v1/recommendations", payload)

    if not rec.get("success"):
        raise HTTPException(status_code=502, detail="Recommendation service returned unsuccessful response")

    output: list[dict[str, Any]] = []
    for item in rec.get("data", []):
        dest = item.get("destination", {})
        output.append(
            {
                "destinationId": dest.get("id"),
                "destinationSlug": dest.get("slug"),
                "destinationName": destination_name(dest.get("name")),
                "score": normalized_score(item.get("score", 0)),
                "matchLevel": item.get("matchLevel"),
                "reason": ", ".join(item.get("reasons", [])[:2]) or f"Matches your {request.mood.lower()} travel preference",
            }
        )

    return {"success": True, "recommendations": output}


@app.post("/api/ai/recommendations/destination")
async def destination_recommendations(request: DestinationRecommendationRequest) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=12.0) as client:
        similar = await get_json(client, f"{RECOMMENDATION_BASE_URL}/api/v1/recommendations/destination/{request.destinationId}/similar")

    if not similar.get("success"):
        raise HTTPException(status_code=502, detail="Recommendation service returned unsuccessful response")

    ranked = []
    pref_tokens = {p.strip().lower() for p in request.preferences if p.strip()}
    for item in similar.get("data", []):
        dest = item.get("destination", {})
        text = " ".join(
            [
                (dest.get("name") or {}).get("defaultName", ""),
                " ".join(item.get("reasons", [])),
                " ".join(item.get("matchedInterests", [])),
            ]
        ).lower()
        bonus = sum(0.08 for token in pref_tokens if token in text)
        ranked.append(
            {
                "destinationId": dest.get("id"),
                "destinationSlug": dest.get("slug"),
                "destinationName": destination_name(dest.get("name")),
                "score": min(1.0, normalized_score(item.get("score", 0)) + bonus),
                "reason": ", ".join(item.get("reasons", [])[:3]) or "Similar travel characteristics",
            }
        )
    ranked.sort(key=lambda x: x["score"], reverse=True)
    return {"success": True, "recommendations": ranked[:12]}


@app.post("/api/ai/recommendations/roadmap")
async def roadmap_recommendations(request: RoadmapRecommendationRequest) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=12.0) as client:
        roadmaps = await get_json(client, f"{ROADMAP_BASE_URL}/api/v1/roadmaps", params={"size": 200})

    if not roadmaps.get("success"):
        raise HTTPException(status_code=502, detail="Roadmap service returned unsuccessful response")

    mood_hint = (request.mood or "").lower()
    region_hint = (request.region or "").lower()
    results = []
    for item in roadmaps.get("data", []):
        name = (item.get("name") or "").lower()
        desc = (item.get("description") or "").lower()
        score = 0.5
        if region_hint and (region_hint in name or region_hint in desc):
            score += 0.35
        if mood_hint and mood_hint in desc:
            score += 0.15
        if request.durationDays:
            estimated_days = max(1, round(float(item.get("totalDistanceKm", 0)) / 120))
            score += max(0.0, 0.2 - (abs(estimated_days - request.durationDays) * 0.03))
        results.append(
            {
                "roadmapId": item.get("id"),
                "roadmapSlug": item.get("slug"),
                "title": item.get("name"),
                "score": round(min(1.0, score), 3),
                "estimatedDistanceKm": item.get("totalDistanceKm"),
                "nodeCount": item.get("nodeCount"),
                "reason": "Matched by region and travel profile",
            }
        )

    results.sort(key=lambda x: x["score"], reverse=True)
    return {"success": True, "recommendations": results[:12]}


@app.post("/api/ai/search")
async def semantic_search(request: SemanticSearchRequest) -> dict[str, Any]:
    query = request.query.lower().strip()
    state_tokens = {
        "rajasthan", "kerala", "karnataka", "maharashtra", "tamil", "goa", "himachal", "uttarakhand",
        "gujarat", "madhya", "odisha", "punjab", "assam", "meghalaya", "sikkim", "ladakh",
    }
    detected_state = next((t for t in state_tokens if t in query), None)

    params: dict[str, Any] = {"size": 250}
    if detected_state:
        params["region"] = detected_state.title()

    async with httpx.AsyncClient(timeout=15.0) as client:
        destinations = await get_json(client, f"{DESTINATION_BASE_URL}/api/v1/destinations", params=params)
        roadmaps = await get_json(client, f"{ROADMAP_BASE_URL}/api/v1/roadmaps", params={"size": 200})
        stories = await get_json(client, f"{STORY_BASE_URL}/api/v1/stories", params={"size": 120})

    if not destinations.get("success") or not roadmaps.get("success") or not stories.get("success"):
        raise HTTPException(status_code=502, detail="One or more upstream services returned unsuccessful response")

    tokens = [t for t in query.replace("-", " ").split() if len(t) >= 2]

    def rank_text(text: str) -> float:
        text_l = text.lower()
        score = 0.0
        for token in tokens:
            if token in text_l:
                score += 0.18
        if "road trip" in query and ("road" in text_l or "trail" in text_l):
            score += 0.22
        if "history" in query and ("heritage" in text_l or "histor" in text_l):
            score += 0.22
        if "food" in query and ("food" in text_l or "cuisine" in text_l):
            score += 0.15
        return score

    items: list[dict[str, Any]] = []

    for d in destinations.get("data", []):
        name = (d.get("name") or {}).get("defaultName", "")
        text = " ".join([name, d.get("state", ""), d.get("region", ""), " ".join(d.get("categories", [])), " ".join(d.get("moods", []))])
        score = rank_text(text)
        if score > 0.05:
            items.append({
                "type": "destination",
                "id": d.get("id"),
                "slug": d.get("slug"),
                "title": name,
                "subtitle": f"{d.get('state')} · {d.get('region')}",
                "score": round(min(1.0, 0.4 + score), 3),
                "path": f"/destinations/{d.get('slug')}",
            })

    for r in roadmaps.get("data", []):
        text = f"{r.get('name','')} {r.get('description','')}"
        score = rank_text(text)
        if score > 0.06:
            items.append({
                "type": "roadmap",
                "id": r.get("id"),
                "slug": r.get("slug"),
                "title": r.get("name"),
                "subtitle": f"{r.get('totalDistanceKm', 0)} km · {r.get('nodeCount', 0)} stops",
                "score": round(min(1.0, 0.35 + score), 3),
                "path": f"/roadmaps/{r.get('slug')}",
            })

    for s in stories.get("data", []):
        text = f"{s.get('title','')} {s.get('shortDescription','')} {s.get('storyType','')}"
        score = rank_text(text)
        if score > 0.06:
            items.append({
                "type": "story",
                "id": s.get("id"),
                "slug": s.get("slug"),
                "title": s.get("title"),
                "subtitle": s.get("storyType"),
                "score": round(min(1.0, 0.3 + score), 3),
                "path": f"/destinations/{s.get('slug','')}",
            })

    items.sort(key=lambda x: x["score"], reverse=True)
    return {"success": True, "query": request.query, "results": items[: request.limit]}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=PORT, reload=False)
