<template>
  <NuxtLoadingIndicator color="#f97316" :height="3" />
  <NuxtRouteAnnouncer />

  <!-- Layout выбирается метаданными страницы: auth для входа, dashboard для кабинета. -->
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>
