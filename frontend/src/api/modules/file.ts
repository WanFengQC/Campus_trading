import http from '../http';
import type { ApiResponse } from '../../types/api';

export interface FileUploadData {
  url: string;
  originalFilename: string;
  size: number;
}

export function uploadAvatar(file: File): Promise<ApiResponse<FileUploadData>> {
  const formData = new FormData();
  formData.append('file', file);
  return http.post('/api/files/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

