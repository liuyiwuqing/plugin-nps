import {definePlugin} from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import {markRaw} from "vue";
import {IconPlug} from "@halo-dev/components";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/nps",
        name: "Nps",
        component: HomeView,
        meta: {
          permissions: ["plugin:nps:view"],
          menu: {
            name: "nps管理",
            group: "tool",
            icon: markRaw(IconPlug),
          },
        },
      },
    },
  ],
});
