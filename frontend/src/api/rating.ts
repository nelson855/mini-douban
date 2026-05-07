import { http } from './http'
import type { RatingResult } from '../types/domain'

export async function rateMovie(movieId: number, score: number) {
  const response = await http.put<RatingResult>(`/api/movies/${movieId}/rating`, { score })
  return response.data
}
