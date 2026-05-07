export interface User {
  id: number
  username: string
  createdAt: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface Movie {
  id: number
  title: string
  director: string | null
  releaseYear: number | null
  posterUrl: string | null
  synopsis: string | null
  averageScore: number | null
  ratingCount: number
  myScore: number | null
}

export type MovieDetail = Movie

export interface RatingResult {
  movieId: number
  myScore: number
  averageScore: number | null
  ratingCount: number
}

export interface ApiError {
  code: string
  message: string
}
