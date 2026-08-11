export interface ApiResponse<T> {
  success: true;
  data: T;
}

export interface ApiError {
  success: false;
  error: {
    code: string;
    message: string;
    details?: string[];
    fields?: Record<string, string>;
  };
}

export type ApiResult<T> = ApiResponse<T> | ApiError;

export interface PageMeta {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Matches the backend PageResponse shape: { success, data: T[], meta: PageMeta } */
export interface PagedApiResponse<T> {
  success: true;
  data: T[];
  meta: PageMeta;
}

/** Alternative paged shape where data wraps content+meta */
export interface PagedData<T> {
  content: T[];
  meta: PageMeta;
}

