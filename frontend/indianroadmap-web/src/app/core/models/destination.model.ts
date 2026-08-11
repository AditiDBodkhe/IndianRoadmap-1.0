export type DestinationCategory = 'VILLAGE' | 'CITY' | 'TEMPLE' | 'MONASTERY' | 'MOUNTAIN' | 'PASS' | 'HERITAGE' | 'HISTORICAL' | 'SCIENTIFIC' | 'ADVENTURE' | 'SPIRITUAL' | 'BORDER';
export type DestinationMood = 'ZEN' | 'ADVENTURE' | 'CURIOUS' | 'HERITAGE' | 'SPIRITUAL' | 'SOLITUDE' | 'WILD' | 'PATRIOTIC';

export interface DestinationName {
  defaultName: string;
  localName?: string;
}

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface Elevation {
  meters: number;
  feet: number;
}

export interface ImageReference {
  url: string;
  caption?: string;
  credit?: string;
  primary?: boolean;
}

export interface DestinationSummary {
  id: string;
  slug: string;
  name: DestinationName;
  state: string;
  region: string;
  coordinates: Coordinates;
  elevation: Elevation;
  categories: DestinationCategory[];
  moods: DestinationMood[];
}

export interface Destination extends DestinationSummary {
  district?: string;
  shortDescription?: string;
  description?: string;
  images: ImageReference[];
  verified: boolean;
  createdAt: string;
  updatedAt: string;
}
