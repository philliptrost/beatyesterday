import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../api/client';
import type { ActivityDetail as ActivityDetailType } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';
import StatCard from '../components/common/StatCard';

export default function ActivityDetail() {
  const { id } = useParams<{ id: string }>();
  const [activity, setActivity] = useState<ActivityDetailType | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    api.getActivity(id)
      .then(setActivity)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <LoadingSpinner />;
  if (!activity) return <div className="text-gray-500">Activity not found.</div>;

  return (
    <div className="space-y-6">
      <div>
        <Link to="/activities" className="text-sm text-gray-500 hover:text-strava">
          &larr; Back to Activities
        </Link>
      </div>

      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">{activity.name}</h1>
          <p className="text-gray-500 mt-1">
            {activity.sportTypeDisplay} &middot;{' '}
            {new Date(activity.startDateTime).toLocaleString()}
            {activity.deviceName && ` &middot; ${activity.deviceName}`}
          </p>
          {activity.description && (
            <p className="text-gray-600 mt-2">{activity.description}</p>
          )}
        </div>
        <a
          href={activity.stravaUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="px-4 py-2 bg-strava text-white rounded-lg text-sm font-medium hover:bg-strava-dark transition-colors"
        >
          View on Strava
        </a>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard label="Distance" value={activity.distanceKm.toFixed(2)} unit="km" />
        <StatCard label="Elevation" value={Math.round(activity.elevationM)} unit="m" />
        <StatCard label="Moving Time" value={activity.movingTimeFormatted} />
        <StatCard label="Avg Speed" value={activity.averageSpeedKmh.toFixed(1)} unit="km/h" />
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {activity.averageHeartRate && (
          <StatCard label="Avg Heart Rate" value={activity.averageHeartRate} unit="bpm" />
        )}
        {activity.maxHeartRate && (
          <StatCard label="Max Heart Rate" value={activity.maxHeartRate} unit="bpm" />
        )}
        {activity.averagePower && (
          <StatCard label="Avg Power" value={activity.averagePower} unit="W" />
        )}
        {activity.maxPower && (
          <StatCard label="Max Power" value={activity.maxPower} unit="W" />
        )}
        {activity.calories && (
          <StatCard label="Calories" value={activity.calories} unit="kcal" />
        )}
        {activity.averageCadence && (
          <StatCard label="Avg Cadence" value={activity.averageCadence} unit="rpm" />
        )}
        <StatCard label="Max Speed" value={activity.maxSpeedKmh.toFixed(1)} unit="km/h" />
        <StatCard label="Kudos" value={activity.kudoCount} />
      </div>
    </div>
  );
}
