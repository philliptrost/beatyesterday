import { useEffect, useState } from 'react';
import { api } from '../api/client';

// MVP settings page — shows Strava connection status.
// TODO: Add unit preferences (metric/imperial), timezone selection, data export options.
export default function Settings() {
  const [authenticated, setAuthenticated] = useState<boolean | null>(null);

  useEffect(() => {
    api.getAuthStatus()
      .then((s) => setAuthenticated(s.authenticated))
      .catch(() => setAuthenticated(false));
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">Settings</h1>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
        <h2 className="text-lg font-semibold">Strava Connection</h2>

        <div className="flex items-center gap-2">
          <div
            className={`w-3 h-3 rounded-full ${
              authenticated ? 'bg-green-500' : 'bg-red-500'
            }`}
          />
          <span className="text-sm">
            {authenticated === null
              ? 'Checking...'
              : authenticated
              ? 'Connected to Strava'
              : 'Not connected'}
          </span>
        </div>

        {!authenticated && authenticated !== null && (
          <p className="text-sm text-gray-500">
            Set your <code className="bg-gray-100 px-1 rounded">STRAVA_CLIENT_ID</code>,{' '}
            <code className="bg-gray-100 px-1 rounded">STRAVA_CLIENT_SECRET</code>, and{' '}
            <code className="bg-gray-100 px-1 rounded">STRAVA_REFRESH_TOKEN</code> environment
            variables to connect.
          </p>
        )}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-4">
        <h2 className="text-lg font-semibold">About</h2>
        <p className="text-sm text-gray-600">
          <strong>Beat Yesterday</strong> is a personal Strava analytics dashboard.
          It imports your activity data from Strava and provides charts, statistics,
          and insights to help you track your fitness progress.
        </p>
        <p className="text-sm text-gray-500">Version 0.1.0 (MVP)</p>
      </div>
    </div>
  );
}
