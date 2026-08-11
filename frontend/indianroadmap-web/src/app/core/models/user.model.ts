export type UserRole = 'USER' | 'ADMIN' | 'CONTENT_EDITOR';
export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  displayName?: string;
  bio?: string;
  profileImageUrl?: string;
  role: UserRole;
  status: AccountStatus;
  createdAt: string;
  lastLoginAt?: string;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  displayName?: string;
}

export interface RefreshRequest {
  refreshToken: string;
}
