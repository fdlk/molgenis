// @flow
import type { State } from './utils/flow.types'

export const SET_STATEMENTS = '__SET_STATEMENTS__'
export const CLEAR_STATEMENTS = '__CLEAR_STATEMENTS__'
export const SET_QUERY = '__SET_QUERY__'

export default {
  [SET_QUERY] (state: State, query: string) {
    state.query = query
  },
  [SET_STATEMENTS] (state: State, statement: string) {
    state.statements = [...state.statements, statement]
  },
  [CLEAR_STATEMENTS] (state: State) {
    state.statements = []
  }

}
