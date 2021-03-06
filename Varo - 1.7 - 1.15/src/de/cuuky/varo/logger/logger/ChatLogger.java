package de.cuuky.varo.logger.logger;

import de.cuuky.varo.logger.Logger;
import de.cuuky.varo.utils.JavaUtils;

public class ChatLogger extends Logger {

	public enum ChatLogType {
		CHAT("CHAT"),
		PRIVATE_CHAT("PRIVATECHAT"),
		TEAMCHAT("TEAMCHAT");

		private String name;

		ChatLogType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static ChatLogType getType(String s) {
			for(ChatLogType type : values())
				if(type.getName().equalsIgnoreCase(s))
					return type;

			return null;
		}
	}

	private static ChatLogger instance;

	private ChatLogger(String name) {
		super(name, false);
	}

	public void println(ChatLogType type, String message) {
		message = JavaUtils.replaceAllColors(message);

		String log = getCurrentDate() + " || " + "[" + type.getName() + "] " + message;

		pw.println(log);
		logs.add(log);

		pw.flush();
	}

	public static ChatLogger getInstance() {
		if(instance == null) {
			instance = new ChatLogger("chatlogs");
		}
		return instance;
	}
}