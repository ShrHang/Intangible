package com.shrhang.intangible;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Client CLIENT;
    public static final Server SERVER;
    static final ModConfigSpec clientSpec;
    static final ModConfigSpec serverSpec;

    static {
        Pair<?, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = (Client) pair.getLeft();
        clientSpec = pair.getRight();
        pair = new ModConfigSpec.Builder().configure(Server::new);
        SERVER = (Server) pair.getLeft();
        serverSpec = pair.getRight();
    }

    public static class Client {
        Client(ModConfigSpec.Builder builder) {}
    }

    public static class Server {
        public final ModConfigSpec.BooleanValue isSlayTheSpire;
        Server(ModConfigSpec.Builder builder) {
            builder.push("features");
            isSlayTheSpire = builder.define("isSlayTheSpire", false);
            builder.pop();
        }
    }
}
