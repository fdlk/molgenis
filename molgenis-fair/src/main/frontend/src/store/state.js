// @flow
import type { State } from './utils/flow.types'

export const INITIAL_STATE = window.__INITIAL_STATE__ || {}

const state: State = {
  message: INITIAL_STATE.message,
  server: 'http://localhost:3000/fdp/fragments',
  statements: [],
  query: 'SELECT * { ?s ?p ?o. ?s ?p ?o } LIMIT 100'
}

export default state
