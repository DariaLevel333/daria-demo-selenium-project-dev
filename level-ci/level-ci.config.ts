import type { Config } from "@level-ci/cli";
export default {
  organization: "ivan-haliurov-levelaccess-com-hspeq",
  project: "demo-selenium-project",
  token: process.env.LEVEL_CI_TOKEN,
  server: "https://api.dev.userway.dev",
  reportPaths: ["./level-ci/level-ci-reports"],
} satisfies Config;
