export type RoadmapStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface RoadmapSummary {
  id: string;
  slug: string;
  name: string;
  description?: string;
  status: RoadmapStatus;
  nodeCount: number;
  edgeCount: number;
  totalDistanceKm: number;
  createdAt: string;
  updatedAt: string;
}

export interface RoadmapNode {
  nodeId: string;
  destinationId: string;
  label: string;
  sequence: number;
  role: 'START' | 'WAYPOINT' | 'MILESTONE' | 'DESTINATION' | 'END';
  elevationMeters: number;
}

export interface RoadmapEdge {
  edgeId: string;
  fromNodeId: string;
  toNodeId: string;
  distanceKm: number;
  estimatedTravelTimeMinutes: number;
  roadType: string;
  difficulty: string;
}

export interface RouteSummary {
  totalDistanceKm: number;
  totalTravelTimeMinutes: number;
  highestElevationMeters: number;
  lowestElevationMeters: number;
  elevationGainMeters: number;
  nodeCount: number;
  edgeCount: number;
}

export interface Roadmap extends RoadmapSummary {
  nodes: RoadmapNode[];
  edges: RoadmapEdge[];
  routeSummary: RouteSummary;
}
