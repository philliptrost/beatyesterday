import type { Athlete } from '../../types';

interface Props {
  athlete: Athlete;
  totalActivities: number;
  totalDistanceKm: number;
  totalMovingTimeHours: number;
}

export default function AthleteProfile({ athlete, totalActivities, totalDistanceKm, totalMovingTimeHours }: Props) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <div className="flex items-center gap-4">
        {athlete.profileImageUrl && (
          <img
            src={athlete.profileImageUrl}
            alt={athlete.fullName}
            className="w-16 h-16 rounded-full"
          />
        )}
        <div>
          <h2 className="text-lg font-semibold">{athlete.fullName}</h2>
          <p className="text-sm text-gray-500">
            {totalActivities} activities &middot;{' '}
            {totalDistanceKm.toLocaleString(undefined, { maximumFractionDigits: 0 })} km &middot;{' '}
            {totalMovingTimeHours.toLocaleString(undefined, { maximumFractionDigits: 0 })} hours
          </p>
        </div>
      </div>
    </div>
  );
}
