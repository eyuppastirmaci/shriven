import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TagResponse } from './url-shortener.service';

export interface CreateTagRequest {
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getTags(): Observable<TagResponse[]> {
    return this.http.get<TagResponse[]>(`${this.apiUrl}/tags`);
  }

  createTag(name: string): Observable<TagResponse> {
    return this.http.post<TagResponse>(`${this.apiUrl}/tags`, { name } as CreateTagRequest);
  }

  deleteTag(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/tags/${id}`);
  }
}
