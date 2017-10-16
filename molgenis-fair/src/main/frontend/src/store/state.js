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
  FILTER CONTAINS(?o, 'Netherlands')
}
LIMIT 10`
  /*
  `PREFIX dbpedia-owl: <http://dbpedia.org/ontology/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?movie ?title ?name
WHERE {
  ?movie dbpedia-owl:starring [ rdfs:label "Brad Pitt"@en ];
         rdfs:label ?title;
         dbpedia-owl:director [ rdfs:label ?name ].
  FILTER LANGMATCHES(LANG(?title), "EN")
  FILTER LANGMATCHES(LANG(?name),  "EN")
}`
   */
}

export default state
