<template>
  <div id="app">
    <UserManager />
  </div>
</template>

<script>
import UserManager from './components/UserManager.vue'

export default {
  name: 'App',
  components: {
    UserManager
  }
}
</script>

<style>
/* 保持全局样式 */
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
  max-width: 800px;
  margin: 60px auto;
}

button:hover {
  background-color: #45a049;
}
</style>
