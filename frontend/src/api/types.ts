export type Role = 'OWNER' | 'ADMIN' | 'STAFF'

export interface AuthUser {
  id: number
  tenantId: number
  fullName: string
  email: string
  role: Role
}

export interface AuthResponse extends AuthUser {
  token: string
}

export interface SignupPayload {
  tenantName: string
  fullName: string
  email: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface Offering {
  id: number
  name: string
  description: string | null
  durationMinutes: number
  price: number
}

export interface OfferingPayload {
  name: string
  description?: string | null
  durationMinutes: number
  price: number
}

export interface User {
  id: number
  fullName: string
  email: string
  role: Role
}

export interface CreateUserPayload {
  fullName: string
  email: string
  password: string
  role: Role
}

export type AppointmentStatus = 'SCHEDULED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED'

export interface Appointment {
  id: number
  serviceId: number
  serviceName: string
  staffId: number
  staffName: string
  customerName: string
  customerEmail: string | null
  customerPhone: string | null
  startAt: string
  endAt: string
  status: AppointmentStatus
}

export interface AppointmentPayload {
  serviceId: number
  staffId: number
  customerName: string
  customerEmail?: string
  customerPhone?: string
  startAt: string
  endAt: string
}
