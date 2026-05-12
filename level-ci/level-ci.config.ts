import type { Config } from "@level-ci/cli";
export default {
  organization: "ivan-haliurov-1-userway-org",
  project: "a11y-selenium-java-sample",
  token: process.env.LEVEL_CI_TOKEN,
  server: "https://api.dev.userway.dev",
  reportPaths: ["./level-ci-reports"],
} satisfies Config;
