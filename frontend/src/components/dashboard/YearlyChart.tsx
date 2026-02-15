import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { YearlyStat } from '../../types';

interface Props {
  data: YearlyStat[];
}

export default function YearlyChart({ data }: Props) {
  const chartData = data.map((y) => ({
    name: y.year.toString(),
    distance: Math.round(y.totalDistanceKm),
    activities: y.activityCount,
  }));

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-sm font-medium text-gray-500 mb-4">Yearly Distance (km)</h3>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="name" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 11 }} />
          <Tooltip
            formatter={(value: number, name: string) => {
              if (name === 'distance') return [`${value} km`, 'Distance'];
              return [`${value}`, 'Activities'];
            }}
          />
          <Bar dataKey="distance" fill="#1e3a5f" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
