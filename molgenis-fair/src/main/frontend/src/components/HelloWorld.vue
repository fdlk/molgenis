<template>
  <div class="row">
    <div class="col text-center">
      <h1>This is the MOLGENIS HelloWorld Component</h1>
      <p>statements: {{ statements }}</p>
      <textarea v-model="query"></textarea>
      <button @click="getFragments" class="btn btn-secondary btn-lg">Click me for a sweet alert</button>
    </div>
  </div>
</template>

<script>
  import { mapState, mapActions, mapMutations } from 'vuex'
  import { GET_FRAGMENTS } from '../store/actions'
  import { SET_QUERY } from '../store/mutations'

  export default {
    name: 'hello-world',
    methods: {
      ...mapActions({
        getFragments: GET_FRAGMENTS
      }),
      ...mapMutations({
        setQuery: SET_QUERY
      })
    },
    computed: {
      query: {
        get () {
          return this.$store.state.query
        },
        set (value) {
          this.setQuery(value)
        }
      },
      ...mapState(['statements'])
    }
  }
</script>
