import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import type { MonthlyStat } from '../../types';

interface Props {
  data: MonthlyStat[];
}

export default function MonthlyChart({ data }: Props) {
  // Shows only the last 12 months to keep the chart readable.
  const chartData = data.slice(-12).map((m) => ({
    name: `${m.monthName.slice(0, 3)} ${m.year}`,
    distance: Math.round(m.totalDistanceKm),
    hours: Math.round(m.totalMovingTimeHours * 10) / 10,
    activities: m.activityCount,
  }));

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-sm font-medium text-gray-500 mb-4">Monthly Distance (km)</h3>
      <ResponsiveContainer width="100%" height={250}>
        <BarChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
          <XAxis dataKey="name" tick={{ fontSize: 11 }} />
          <YAxis tick={{ fontSize: 11 }} />
          <Tooltip
            formatter={(value: number, name: string) => {
              if (name === 'distance') return [`${value} km`, 'Distance'];
              return [value, name];
            }}
          />
          <Bar dataKey="distance" fill="#FC4C02" radius={[4, 4, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
