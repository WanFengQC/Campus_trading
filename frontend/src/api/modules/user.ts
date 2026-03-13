import http from '../http';
import type { ApiResponse } from '../../types/api';

export interface UserProfileData {
  userId: number;
  username: string;
  nickname: string;
  avatarUrl?: string;
  status: number;
}

export interface UpdateMyProfilePayload {
  nickname: string;
  avatarUrl?: string;
}

export function getMyProfile(): Promise<ApiResponse<UserProfileData>> {
  return http.get('/api/users/me');
}

export function updateMyProfile(payload: UpdateMyProfilePayload): Promise<ApiResponse<UserProfileData>> {
  return http.put('/api/users/me', payload);
}
