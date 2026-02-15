import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Dashboard as DashboardData } from '../types';
import LoadingSpinner from '../components/common/LoadingSpinner';
import StatCard from '../components/common/StatCard';
import AthleteProfile from '../components/dashboard/AthleteProfile';
import RecentActivities from '../components/dashboard/RecentActivities';
import MonthlyChart from '../components/dashboard/MonthlyChart';
import YearlyChart from '../components/dashboard/YearlyChart';
import SportBreakdown from '../components/dashboard/SportBreakdown';

export default function Dashboard() {
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch dashboard data on mount — single API call gets everything (athlete, stats, recent activities).
  useEffect(() => {
    api.getDashboard()
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  // Triggers a full Strava sync, then refreshes the dashboard to show new data.
  const handleImport = async () => {
    setImporting(true);
    try {
      await api.triggerImport();
      // Reload dashboard after import
      const fresh = await api.getDashboard();
      setData(fresh);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Import failed');
    } finally {
      setImporting(false);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <div className="text-red-600 p-4">Error: {error}</div>;
  if (!data) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <button
          onClick={handleImport}
          disabled={importing}
          className="px-4 py-2 bg-strava text-white rounded-lg text-sm font-medium hover:bg-strava-dark disabled:opacity-50 transition-colors"
        >
          {importing ? 'Importing...' : 'Sync from Strava'}
        </button>
      </div>

      {data.athlete && (
        <AthleteProfile
          athlete={data.athlete}
          totalActivities={data.totalActivities}
          totalDistanceKm={data.totalDistanceKm}
          totalMovingTimeHours={data.totalMovingTimeHours}
        />
      )}

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatCard label="Total Activities" value={data.totalActivities} />
        <StatCard label="Total Distance" value={Math.round(data.totalDistanceKm)} unit="km" />
        <StatCard label="Total Elevation" value={Math.round(data.totalElevationM)} unit="m" />
        <StatCard label="Total Time" value={Math.round(data.totalMovingTimeHours)} unit="hours" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <MonthlyChart data={data.monthlyStats} />
        <YearlyChart data={data.yearlyStats} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SportBreakdown data={data.sportBreakdown} />
        <RecentActivities activities={data.recentActivities} />
      </div>
    </div>
  );
}
