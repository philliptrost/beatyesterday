import { Link } from 'react-router-dom';
import type { ActivitySummary } from '../../types';

interface Props {
  activities: ActivitySummary[];
}

export default function RecentActivities({ activities }: Props) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-sm font-medium text-gray-500 mb-4">Recent Activities</h3>
      <div className="space-y-3">
        {activities.map((activity) => (
          <Link
            key={activity.id}
            to={`/activities/${activity.id}`}
            className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div>
              <p className="font-medium text-sm">{activity.name}</p>
              <p className="text-xs text-gray-500">
                {activity.sportTypeDisplay} &middot;{' '}
                {new Date(activity.startDateTime).toLocaleDateString()}
              </p>
            </div>
            <div className="text-right">
              <p className="text-sm font-medium">
                {activity.distanceKm.toFixed(1)} km
              </p>
              <p className="text-xs text-gray-500">{activity.movingTimeFormatted}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
