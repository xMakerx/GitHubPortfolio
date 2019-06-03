package org.xmakerx.raidpractice.util;

import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayOutChat;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R2.PacketPlayOutTitle.EnumTitleAction;

import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TextUtil {
	
	public static void sendActionBarMsg(Player player, String message) {
		sendPacket(player, new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2));
	}
	
	public static void sendTitle(Player player, String text) {
		sendPacket(player, new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\":\"" + text + "\"}")));
	}
	
	public static void sendTitle(Player player, String text, String footerText, int fadeInTime, int screenTime, int fadeOutTime) {
		final IChatBaseComponent header = ChatSerializer.a("{\"text\":\"" + text + "\"}");
		final IChatBaseComponent footer = ChatSerializer.a("{\"text\":\"" + footerText + "\"}");
		final PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, header, fadeInTime, screenTime, fadeOutTime);
		final PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, footer);
		sendPacket(player, titlePacket);
		sendPacket(player, subtitlePacket);
	}
	
	public static void sendSubtitle(Player player, String text) {
		sendPacket(player, new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\"" + text + "\"}")));
	}
	
	public static void sendSubtitle(Player player, String text, int fadeInTime, int screenTime, int fadeOutTime) {
		sendPacket(player, new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\"" + text + "\"}"), fadeInTime, screenTime, fadeOutTime));
	}
	
	private static void sendPacket(Player player, Packet<?> packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}
}
