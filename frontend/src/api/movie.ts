import { http } from './http'
import type { Movie, MovieDetail } from '../types/domain'

export async function listMovies() {
  const response = await http.get<Movie[]>('/api/movies')
  return response.data
}

export async function getMovie(id: number) {
  const response = await http.get<MovieDetail>(`/api/movies/${id}`)
  return response.data
}
