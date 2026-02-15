import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { GearItem } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';

export default function Gear() {
  const [gear, setGear] = useState<GearItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getGear()
      .then(setGear)
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  if (gear.length === 0) {
    return (
      <div className="space-y-4">
        <h1 className="text-2xl font-bold">Gear</h1>
        <p className="text-gray-500">No gear imported yet. Sync from Strava on the Dashboard to import your gear.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Gear</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {gear.map((item) => (
          <div
            key={item.id}
            className={`bg-white rounded-xl shadow-sm border border-gray-100 p-5 ${
              item.isRetired ? 'opacity-60' : ''
            }`}
          >
            <div className="flex items-center justify-between mb-2">
              <h3 className="font-semibold">{item.name}</h3>
              {item.isRetired && (
                <span className="text-xs bg-gray-200 text-gray-600 px-2 py-0.5 rounded-full">
                  Retired
                </span>
              )}
            </div>
            <p className="text-2xl font-bold text-strava">
              {item.distanceKm.toLocaleString(undefined, { maximumFractionDigits: 0 })}
              <span className="text-sm text-gray-400 ml-1">km</span>
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}
