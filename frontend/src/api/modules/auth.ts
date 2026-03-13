import http from '../http';
import type { ApiResponse } from '../../types/api';

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload extends LoginPayload {
  nickname?: string;
}

export interface AuthTokenData {
  token: string;
  tokenType: string;
  expireSeconds: number;
  userId: number;
  username: string;
  nickname: string;
}

export function login(payload: LoginPayload): Promise<ApiResponse<AuthTokenData>> {
  return http.post('/api/auth/login', payload);
}

export function register(payload: RegisterPayload): Promise<ApiResponse<AuthTokenData>> {
  return http.post('/api/auth/register', payload);
}
