import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import type { SportBreakdown as SportBreakdownType } from '../../types';

interface Props {
  data: SportBreakdownType[];
}

// Chart colors — Strava orange first, then a palette of distinct colors.
const COLORS = ['#FC4C02', '#1e3a5f', '#2563eb', '#16a34a', '#d97706', '#dc2626', '#7c3aed', '#0891b2'];

export default function SportBreakdown({ data }: Props) {
  // Limits to top 8 sport types to keep the pie chart readable. Remaining types are excluded.
  const chartData = data.slice(0, 8).map((s) => ({
    name: s.displayName,
    value: s.activityCount,
  }));

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-sm font-medium text-gray-500 mb-4">Activity Types</h3>
      <ResponsiveContainer width="100%" height={250}>
        <PieChart>
          <Pie
            data={chartData}
            cx="50%"
            cy="50%"
            innerRadius={50}
            outerRadius={90}
            paddingAngle={2}
            dataKey="value"
          >
            {chartData.map((_, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip formatter={(value: number) => [`${value} activities`]} />
          <Legend fontSize={12} />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
