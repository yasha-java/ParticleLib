package org.liny.particlelib;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Color;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class ParticleLib {

    public static void drawSphereHigh (Location loc, Color color, int radius, int h) {

        for (double t = 0; t < 2 * Math.PI; t += Math.PI / 16) {
            double x_offset = radius * Math.cos(t);
            double z_offset = radius * Math.sin(t);
            double y_offset = h * (t / Math.PI);

            ClientboundLevelParticlesPacket particle_packet = new ClientboundLevelParticlesPacket(
                    new DustParticleOptions(new Vector3f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F), 1),
                    true,
                    loc.getX() + x_offset,
                    loc.getY() + y_offset,
                    loc.getZ() + z_offset,
                    (float) 0,
                    (float) 0,
                    (float) 0,
                    0,
                    1
                    );

            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNull(loc.getWorld()).getName()))
                    .forEach(player -> ((CraftPlayer)player).getHandle().connection.send(particle_packet));

        }

    }

    public static void drawSphere (Location loc, Color color, int radius) {

        for (double t = 0; t < 2 * Math.PI; t += Math.PI / 16) {
            double x_offset = radius * Math.cos(t);
            double z_offset = radius * Math.sin(t);

            ClientboundLevelParticlesPacket particle_packet = new ClientboundLevelParticlesPacket(
                    new DustParticleOptions(new Vector3f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F), 1),
                    true,
                    loc.getX() + x_offset,
                    loc.getY() + 0,
                    loc.getZ() + z_offset,
                    (float) 0,
                    (float) 0,
                    (float) 0,
                    0,
                    1
            );

            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(player -> player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNull(loc.getWorld()).getName()))
                    .forEach(player -> ((CraftPlayer)player).getHandle().connection.send(particle_packet));

        }

    }

    public static class drawSphereAsync {

        private double t = 0;
        private final double limiter = 2 * Math.PI;
        private final double adder = Math.PI / 16;

        private int task_id = Integer.MAX_VALUE;

        public drawSphereAsync (Plugin main, Location loc, Color color, int radius, int h, int particle_size, int speed) {

            this.task_id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, () -> {

                double x_offset = radius * Math.cos(t);
                double z_offset = radius * Math.sin(t);
                double y_offset = h * (t / Math.PI);

                ClientboundLevelParticlesPacket particle_packet = new ClientboundLevelParticlesPacket(
                        new DustParticleOptions(new Vector3f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F), particle_size),
                        true,
                        loc.getX() + x_offset,
                        loc.getY() + y_offset,
                        loc.getZ() + z_offset,
                        (float) 0,
                        (float) 0,
                        (float) 0,
                        0,
                        1
                );

                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNull(loc.getWorld()).getName()))
                        .forEach(player -> ((CraftPlayer)player).getHandle().connection.send(particle_packet));

                this.t += this.adder;

                if (this.t >= this.limiter) Bukkit.getScheduler().cancelTask(this.task_id);

            }, 1, speed);

        }

        public drawSphereAsync (Plugin main, Location loc, Color color, int radius, int particleSize, int speed) {

            this.task_id = Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, () -> {

                double x_offset = radius * Math.cos(t);
                double z_offset = radius * Math.sin(t);

                ClientboundLevelParticlesPacket particle_packet = new ClientboundLevelParticlesPacket(
                        new DustParticleOptions(new Vector3f((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F), particleSize),
                        true,
                        loc.getX() + x_offset,
                        loc.getY() + 0,
                        loc.getZ() + z_offset,
                        (float) 0,
                        (float) 0,
                        (float) 0,
                        0,
                        1
                );

                Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNull(loc.getWorld()).getName()))
                        .forEach(player -> ((CraftPlayer)player).getHandle().connection.send(particle_packet));

                this.t += this.adder;

                if (this.t >= this.limiter) Bukkit.getScheduler().cancelTask(this.task_id);

            }, 1, speed);

        }

    }

    public static class startSphereOfEntity {

        LivingEntity targetEntity;
        int radius;
        int slices; // количество "срезов" (вертикальных частей) сферы
        int stacks; // количество "колец" (горизонтальных частей) сферы
        Consumer<Class<?>> whencomplete;
        int taskId;
        List<ImmutableTriple<Double, Double, Double>> particles = new LinkedList<>();

        public startSphereOfEntity (LivingEntity targetEntity, int radius, int slices, int stacks) {
            this.targetEntity = targetEntity;
            this.radius = radius;
            this.slices = slices;
            this.stacks = stacks;
        }

        public startSphereOfEntity setWhencomplete(Consumer<Class<?>> whencomplete) {
            this.whencomplete = whencomplete;
            return this;
        }

        public void start (Color color, Plugin main, int speed) {

            AtomicInteger i = new AtomicInteger(0);

            this.taskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, () -> {

                double theta = Math.PI * i.getAndAdd(1) / slices;
                double sinTheta = Math.sin(theta);
                double cosTheta = Math.cos(theta);

                for (int j = 0; j <= stacks; j++) {
                    double phi = Math.PI * 2 * j / stacks;
                    double sinPhi = Math.sin(phi);
                    double cosPhi = Math.cos(phi);

                    this.particles.add(new ImmutableTriple<>(
                            radius * sinTheta * cosPhi,
                            radius * cosTheta,
                            radius * sinTheta * sinPhi
                    ));

                    if (i.get() >= slices) {
                        try {
                            if (this.whencomplete != null)
                                Bukkit.getScheduler().runTask(main, () -> {
                                    this.whencomplete.accept(null);
                                });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Bukkit.getScheduler().cancelTask(this.taskId);
                    }

                }

                List<Player> playerOnline = Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getWorld().getName().equalsIgnoreCase(Objects.requireNonNull(targetEntity.getLocation().getWorld()).getName()))
                        .collect(Collectors.toList());

                Location loc = targetEntity.getLocation().clone().add(0, 1, 0);

                particles.forEach(packet -> {
                    ClientboundLevelParticlesPacket particlePacket = new ClientboundLevelParticlesPacket(
                            new DustParticleOptions(new Vector3f((float) color.getRed() / 255.0F, (float) color.getGreen() / 255.0F, (float) color.getBlue() / 255.0F), 1),
                            true,
                            loc.getX() + packet.left,
                            loc.getY() + packet.middle,
                            loc.getZ() + packet.right,
                            0,
                            0,
                            0,
                            0,
                            1
                    );
                    playerOnline.forEach(player -> {
                        ((CraftPlayer) player).getHandle().connection.send(particlePacket);
                    });
                });

            }, 1, speed);

        }

    }

}
