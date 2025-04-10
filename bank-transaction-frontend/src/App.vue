<template>
  <el-container>
    <el-header>
      <h1>Bank Transaction Management System</h1>
    </el-header>
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
/**
 * Root component of the application
 * Provides the basic layout structure with header and main content area
 */
export default {
  name: 'App'
}
</script>

<style>
/* Header styling */
.el-header {
  background-color: #409EFF;
  color: white;
  text-align: center;
  line-height: 60px;
}

.el-header h1 {
  margin: 0;
}

/* Main content area styling */
.el-main {
  padding: 20px;
  max-width: 1700px;
  margin: 0 auto;
}
</style> 