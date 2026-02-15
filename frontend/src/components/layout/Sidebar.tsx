import { NavLink } from 'react-router-dom';

// Navigation items for the sidebar. The 'end' prop on NavLink ensures '/' only matches exactly (not '/activities').
const navItems = [
  { to: '/', label: 'Dashboard', icon: '📊' },
  { to: '/activities', label: 'Activities', icon: '🏃' },
  { to: '/gear', label: 'Gear', icon: '⚙️' },
  { to: '/settings', label: 'Settings', icon: '🔧' },
];

export default function Sidebar() {
  return (
    <aside className="w-64 bg-gray-900 text-white flex flex-col">
      <div className="p-6 border-b border-gray-700">
        <h1 className="text-xl font-bold text-strava">Beat Yesterday</h1>
        <p className="text-xs text-gray-400 mt-1">Strava Analytics</p>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${
                isActive
                  ? 'bg-strava text-white'
                  : 'text-gray-300 hover:bg-gray-800 hover:text-white'
              }`
            }
          >
            <span>{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-gray-700 text-xs text-gray-500">
        Powered by Strava API
      </div>
    </aside>
  );
}
