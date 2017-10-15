// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  message: INITIAL_STATE.message,
  servers: ['http://localhost:3000/fdp/fragments', 'http://fragments.dbpedia.org/2016-04/en'],
  statements: [],
  query: `SELECT * 
WHERE {
  ?s ?p ?o .
  #FILTER CONTAINS(?o, 'Netherlands')
}
LIMIT 10`
}

export default state
