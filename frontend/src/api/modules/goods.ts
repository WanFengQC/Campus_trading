import http from '../http';
import type { ApiResponse } from '../../types/api';

export function getGoodsList(params?: { keyword?: string; categoryId?: number }): Promise<ApiResponse<unknown>> {
  return http.get('/api/goods', { params });
}

export function getGoodsDetail(id: string | number): Promise<ApiResponse<unknown>> {
  return http.get(`/api/goods/${id}`);
}

// TODO: add create/update/off-shelf APIs.
