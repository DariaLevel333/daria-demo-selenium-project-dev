import type { Config } from "@level-ci/cli";
export default {
  organization: "daria-osokina-1-levelaccess-com-kofcj",
  project: "test-website-1-mirko-github-daria",
  token: process.env.LEVEL_CI_TOKEN,
  server: "https://api.dev.userway.dev",
  reportPaths: ["./level-ci/level-ci-reports"],
} satisfies Config;
