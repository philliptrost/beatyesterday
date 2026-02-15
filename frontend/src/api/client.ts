/**
 * Centralized API client — all backend calls go through here.
 * The /api prefix is proxied to localhost:8080 by Vite during development (see vite.config.ts).
 * In production, the frontend is served from the same origin so no proxy is needed.
 */
import type { ActivityDetail, Dashboard, GearItem, Page, ActivitySummary } from '../types';

const API_BASE = '/api';

/**
 * Generic fetch wrapper with JSON parsing and error handling.
 * All API methods return typed responses.
 */
async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${url}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!response.ok) {
    throw new Error(`API error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}

export const api = {
  // Dashboard
  getDashboard: () => fetchJson<Dashboard>('/dashboard'),

  // Activities
  getActivities: (page = 0, size = 50, sort = 'startDateTime', direction = 'desc', sportType?: string) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sort,
      direction,
    });
    if (sportType) params.set('sportType', sportType);
    return fetchJson<Page<ActivitySummary>>(`/activities?${params}`);
  },

  getActivity: (id: string) => fetchJson<ActivityDetail>(`/activities/${id}`),

  // Gear
  getGear: () => fetchJson<GearItem[]>('/gear'),

  // Import
  triggerImport: () =>
    fetchJson<{ success: boolean; message: string }>('/import', { method: 'POST' }),

  // OAuth
  getAuthStatus: () => fetchJson<{ authenticated: boolean }>('/oauth/strava/status'),
  getAuthUrl: (redirectUri: string) =>
    fetchJson<{ url: string }>(`/oauth/strava/url?redirectUri=${encodeURIComponent(redirectUri)}`),
};
