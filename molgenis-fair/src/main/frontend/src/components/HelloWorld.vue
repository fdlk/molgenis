<template>
  <div>
    <div class="row">
      <div class="col">
        <h1 class="pt-3">Query Linked Data Frament servers</h1>
        <form>
          <div class="form-group">
            <label for="servers">Servers</label>
            <ul id="servers">
              <li v-for="server in servers">{{server}}</li>
            </ul>
          </div>
          <div class="form-group">
            <label for="queryInput">Query</label>
            <textarea v-model="query" class="form-control" id="queryInput" rows="7"></textarea>
          </div>
          <button @click.prevent="getFragments" class="btn btn-primary">Query</button>
        </form>
      </div>
    </div>
    <div class="row pt-3">
      <div class="col">
        <table v-if="statements.length" class="table table-striped table-sm table-responsive">
          <thead class="thead-inverse">
          <th>#</th>
          <th v-for="key in Object.keys(statements[0])">
            {{key}}
          </th>
          </thead>
          <tbody>
          <tr v-for="(statement, index) in statements">
            <td>{{index}}</td>
            <td v-for="key in Object.keys(statement)">
              <code>{{statement[key]}}</code>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
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
      ...mapState(['statements', 'servers'])
    }
  }
</script>
