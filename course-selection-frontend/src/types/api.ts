export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageRequest {
  page?: number
  size?: number
  sort?: string
  direction?: 'asc' | 'desc'
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  numberOfElements: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface ApiError {
  code: number
  message: string
  timestamp: number
}