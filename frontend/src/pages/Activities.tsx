import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { ActivitySummary, Page } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';

export default function Activities() {
  const [data, setData] = useState<Page<ActivitySummary> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [sportFilter, setSportFilter] = useState<string>('');

  // Re-fetches when page or sport filter changes. The backend handles sorting and pagination.
  useEffect(() => {
    setLoading(true);
    api.getActivities(page, 50, 'startDateTime', 'desc', sportFilter || undefined)
      .then(setData)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [page, sportFilter]);

  if (loading && !data) return <LoadingSpinner />;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Activities</h1>
        {/* MVP: hardcoded sport types. TODO: Fetch available sport types from the backend dynamically. */}
        <select
          value={sportFilter}
          onChange={(e) => { setSportFilter(e.target.value); setPage(0); }}
          className="text-sm border border-gray-300 rounded-lg px-3 py-2"
        >
          <option value="">All Sports</option>
          <option value="Ride">Ride</option>
          <option value="Run">Run</option>
          <option value="Swim">Swim</option>
          <option value="Walk">Walk</option>
          <option value="Hike">Hike</option>
          <option value="WeightTraining">Weight Training</option>
          <option value="Yoga">Yoga</option>
        </select>
      </div>

      {data && (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-gray-50 text-left text-xs text-gray-500 uppercase tracking-wider">
                  <th className="px-4 py-3">Date</th>
                  <th className="px-4 py-3">Name</th>
                  <th className="px-4 py-3">Type</th>
                  <th className="px-4 py-3 text-right">Distance</th>
                  <th className="px-4 py-3 text-right">Elevation</th>
                  <th className="px-4 py-3 text-right">Time</th>
                  <th className="px-4 py-3 text-right">Speed</th>
                  <th className="px-4 py-3 text-right">HR</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {data.content.map((activity) => (
                  <tr key={activity.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-4 py-3 text-gray-500">
                      {new Date(activity.startDateTime).toLocaleDateString()}
                    </td>
                    <td className="px-4 py-3">
                      <Link
                        to={`/activities/${activity.id}`}
                        className="text-strava hover:underline font-medium"
                      >
                        {activity.name}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-gray-500">{activity.sportTypeDisplay}</td>
                    <td className="px-4 py-3 text-right">{activity.distanceKm.toFixed(1)} km</td>
                    <td className="px-4 py-3 text-right">{activity.elevationM.toFixed(0)} m</td>
                    <td className="px-4 py-3 text-right">{activity.movingTimeFormatted}</td>
                    <td className="px-4 py-3 text-right">{activity.averageSpeedKmh.toFixed(1)} km/h</td>
                    <td className="px-4 py-3 text-right text-gray-500">
                      {activity.averageHeartRate ?? '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between text-sm">
            <p className="text-gray-500">
              Showing {data.number * data.size + 1}-{Math.min((data.number + 1) * data.size, data.totalElements)} of {data.totalElements}
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={data.number === 0}
                className="px-3 py-1 border rounded-lg disabled:opacity-50 hover:bg-gray-50"
              >
                Previous
              </button>
              <button
                onClick={() => setPage((p) => p + 1)}
                disabled={data.number >= data.totalPages - 1}
                className="px-3 py-1 border rounded-lg disabled:opacity-50 hover:bg-gray-50"
              >
                Next
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
