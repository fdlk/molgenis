export const GET_FRAGMENTS = '__GET_FRAGMENTS__'
import { CLEAR_STATEMENTS, SET_STATEMENTS } from './mutations'

export default {
  /**
   * Example action for retrieving all EntityTypes from the server
   */
  [GET_FRAGMENTS] ({commit, state}) {
    const ldf = window.ldf
    const fragmentsClient = new ldf.FragmentsClient(state.servers)

    commit(CLEAR_STATEMENTS)
    const results = new ldf.SparqlIterator(state.query, {fragmentsClient: fragmentsClient})
    results.on('data', function (result) {
      console.log('result', result)
      commit(SET_STATEMENTS, result)
    })
  }
}
