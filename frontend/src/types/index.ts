// Types matching backend DTOs

export interface ActivitySummary {
  id: string;
  startDateTime: string;
  sportType: string;
  sportTypeDisplay: string;
  activityType: string;
  name: string;
  distanceKm: number;
  elevationM: number;
  averageSpeedKmh: number;
  averageHeartRate: number | null;
  averagePower: number | null;
  movingTimeSeconds: number;
  movingTimeFormatted: string;
  kudoCount: number;
  isCommute: boolean;
  gearId: string | null;
  stravaUrl: string;
}

export interface ActivityDetail extends ActivitySummary {
  description: string | null;
  calories: number | null;
  maxPower: number | null;
  maxSpeedKmh: number;
  maxHeartRate: number | null;
  averageCadence: number | null;
  deviceName: string | null;
  polyline: string | null;
  startLatitude: number | null;
  startLongitude: number | null;
}

export interface Dashboard {
  athlete: Athlete | null;
  recentActivities: ActivitySummary[];
  monthlyStats: MonthlyStat[];
  yearlyStats: YearlyStat[];
  sportBreakdown: SportBreakdown[];
  totalActivities: number;
  totalDistanceKm: number;
  totalElevationM: number;
  totalMovingTimeHours: number;
}

export interface Athlete {
  id: string;
  firstName: string;
  lastName: string;
  fullName: string;
  profileImageUrl: string | null;
}

export interface MonthlyStat {
  year: number;
  month: number;
  monthName: string;
  activityCount: number;
  totalDistanceKm: number;
  totalElevationM: number;
  totalMovingTimeHours: number;
}

export interface YearlyStat {
  year: number;
  activityCount: number;
  totalDistanceKm: number;
  totalElevationM: number;
  totalMovingTimeHours: number;
}

export interface SportBreakdown {
  sportType: string;
  displayName: string;
  activityCount: number;
  totalDistanceKm: number;
  totalMovingTimeHours: number;
}

export interface GearItem {
  id: string;
  name: string;
  distanceKm: number;
  distanceM: number;
  isRetired: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
