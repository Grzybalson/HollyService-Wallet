package dev.grzybalson.config;

import eu.okaeri.configs.OkaeriConfig;
import java.util.*;

public class WalletConfig extends OkaeriConfig {
    public int rows = 5;
    public String title = "&8ᴘᴏʀᴛꜰᴇʟ &8• &cHollyService";
    public String backgroundItemName = "&c";
    public boolean hideAttributes = true;
    public Map<String, List<Integer>> template54 = decorations();
    public Map<String, Integer> backgroundCustomModelData = backgroundModels();
    public ItemConfig dailyItem = new ItemConfig(12, "CLOCK",
        "&#00FF64&lᴄ&#0DFE6C&lᴏ&#1AFE73&lᴅ&#27FD7B&lᴢ&#34FD83&lɪ&#41FC8A&lᴇ&#4EFB92&lɴ&#5BFB99&lɴ&#68FAA1&lᴀ &#4EFB92&lɴ&#41FC8A&lᴀ&#34FD83&lɢ&#27FD7B&lʀ&#1AFE73&lᴏ&#0DFE6C&lᴅ&#00FF64&lᴀ",
        "&f", "&8⁎ &fᴢᴀᴋʀᴇꜱ ᴅᴀʀᴍᴏᴡʏᴄʜ ᴡᴘʟɴ: &#68FAA1%reward_min% - %reward_max% ᴡᴘʟɴ", "&f",
        "&#00FF64&nᴋ&#0AFF6A&nʟ&#15FE70&nɪ&#1FFE76&nᴋ&#2AFD7C&nɴ&#34FD83&nɪ&#3EFC89&nᴊ&#49FC8F&n, &#5EFB9B&nᴀ&#68FAA1&nʙ&#5CFB9A&nʏ &#45FC8D&nᴏ&#3AFC86&nᴅ&#2EFD7F&nᴇ&#23FD78&nʙ&#17FE72&nʀ&#0CFE6B&nᴀ&#00FF64&nᴄ");
    public ItemConfig informationItem = new ItemConfig(14, "WRITABLE_BOOK",
        "&#FF4800&lɪ&#FD5310&lɴ&#FB5E20&lꜰ&#F96930&lᴏ&#F77440&lʀ&#F57F50&lᴍ&#F8713C&lᴀ&#FA6428&lᴄ&#FD5614&lᴊ&#FF4800&lᴇ",
        "&f", "&8⁎ &fᴘᴏʀᴛꜰᴇʟ ᴜᴍᴏᴢʟɪᴡɪᴀ ᴘʀᴢᴇᴄʜᴏᴡʏᴡᴀɴɪᴇ", "&8⁎ &fꜱʀᴏᴅᴋᴏᴡ ɴᴀ ᴢᴀᴋᴜᴘ ʀᴢᴇᴄᴢʏ", "&f",
        "&fᴅᴏʟᴀᴅᴜᴊ ᴘᴏʀᴛꜰᴇʟ ɴᴀ ꜱᴛʀᴏɴɪᴇ:",
        "&#FF0000ᴡ&#FF0B0Bᴡ&#FF1616ᴡ&#FF2121.&#FF2C2Cʜ&#FF3636ᴏ&#FF4141ʟ&#FF4C4Cʟ&#FF5757ʏ&#FF6262ꜱ&#FF5757ᴇ&#FF4C4Cʀ&#FF4141ᴠ&#FF3636ɪ&#FF2C2Cᴄ&#FF2121ᴇ&#FF1616.&#FF0B0Bᴘ&#FF0000ʟ",
        "&f", "&8⁎ &fꜱᴛᴀɴ ᴋᴏɴᴛᴀ &#FF0000%hollywallet_wpln% ᴡᴘʟɴ");
    public ItemConfig backItem = new ItemConfig(40, "BARRIER",
        "&#FFF800&lᴘ&#FEF80C&lᴏ&#FEF717&lᴡ&#FDF723&lʀ&#FCF62F&ló&#FBF63A&lᴛ &#FAF552&lᴅ&#F9F55D&lᴏ &#F8F475&lꜱ&#F7F380&lᴛ&#F6F38C&lʀ&#F7F37F&lᴏ&#F8F473&lɴ&#F8F466&lʏ &#FAF54C&lɢ&#FBF640&lʟ&#FCF633&ló&#FDF726&lᴡ&#FDF719&lɴ&#FEF80D&lᴇ&#FFF800&lᴊ",
        "&8⁎ &fᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴘʀᴢᴇᴊꜱᴄ");
    public Reward reward = new Reward();
    public Confirmation confirmation = new Confirmation();
    public Logs logs = new Logs();
    public Broadcast topup = new Broadcast();
    public Messages messages = new Messages();
    public FeedbackSounds sounds = new FeedbackSounds();
    public Map<String, Category> categories = categories();
    public String timezone = "Europe/Warsaw";

    public static class Reward extends OkaeriConfig {
        public boolean enabled = true;
        public String min = "0.10";
        public String max = "0.50";
        public long cooldownHours = 24;
    }

    public static class Category extends OkaeriConfig {
        public ItemConfig item = new ItemConfig(28, "BOOK",
            "&#FF0000&lʀ&#FF1616&lᴀ&#FF2B2B&lɴ&#FF1616&lɢ&#FF0000&lɪ",
            "&f", "&8⁎ &fᴢᴀᴋᴜᴘɪꜱᴢ ᴛᴜᴛᴀᴊ ʀᴏᴢɴᴇ &#FF2B2Bʀᴀɴɢɪ", "&f",
            "&#FF0000&nᴋ&#FF0404&nʟ&#FF0909&nɪ&#FF0D0D&nᴋ&#FF1111&nɴ&#FF1616&nɪ&#FF1A1A&nᴊ&#FF1E1E&n, &#FF2727&nᴀ&#FF2B2B&nʙ&#FF2626&nʏ &#FF1D1D&nᴘ&#FF1818&nʀ&#FF1313&nᴢ&#FF0E0E&nᴇ&#FF0A0A&nᴊ&#FF0505&nꜱ&#FF0000&nᴄ");
        public String title = "&8ᴘᴏʀᴛꜰᴇʟ &8• &cʀᴀɴɢɪ";
        public Map<String, Product> products = products();
    }

    public static class Product extends OkaeriConfig {
        public String price = "10.00";
        public ItemConfig item = new ItemConfig(22, "LEATHER_HELMET", "&fʀᴀɴɢᴀ &a&lᴠɪᴘ",
            "&8ɪɴꜰᴏʀᴍᴀᴄᴊᴇ ᴏ ᴘʀᴏᴅᴜᴋᴄɪᴇ", "&f", "&8⁎  &fᴋᴏꜱᴢᴛ: &a%holly_cost% ᴡᴘʟɴ",
            "&8⁎  &fᴄᴢᴀꜱ ᴛʀᴡᴀɴɪᴀ: &e30dni", "&f", "&8⁎ &fᴡɪᴇᴄᴇᴊ ɪɴꜰᴏʀᴍᴀᴄᴊɪ", "&8⁎ &fᴘᴏᴅ: &6/ʀᴀɴɢɪ",
            "&f", "&a&nᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴡʏʙʀᴀᴄ", "&f");
        public List<String> commands = new ArrayList<>(Collections.singletonList("lp user %player% parent addtemp vip 30d"));
    }

    public static class Confirmation extends OkaeriConfig {
        public String title = "&8ᴘᴏᴛᴡɪᴇʀᴅᴢ ᴢᴀᴋᴜᴘ";
        public int previewSlot = 22;
        public ItemConfig confirm = new ItemConfig(20, "LIME_STAINED_GLASS_PANE", "&a&lᴘᴏᴛᴡɪᴇʀᴅᴢ ᴢᴀᴋᴜᴘ",
            "&f", "&8⁎ &fᴋᴏꜱᴢᴛ: &a%holly_cost% ᴡᴘʟɴ", "&8⁎ &fꜱᴀʟᴅᴏ: &e%hollywallet_wpln% ᴡᴘʟɴ", "&f", "&aᴋʟɪᴋɴɪᴊ, ᴀʙʏ ᴋᴜᴘɪᴄ");
        public ItemConfig cancel = new ItemConfig(24, "RED_STAINED_GLASS_PANE", "&c&lᴀɴᴜʟᴜᴊ", "&8⁎ &fᴘᴏᴡʀᴏᴛ ᴅᴏ ᴋᴀᴛᴇɢᴏʀɪɪ");
    }

    public static class Logs extends OkaeriConfig {
        public String title = "&8ʜɪꜱᴛᴏʀɪᴀ &8• &c%player% &8(%page%)";
        public List<Integer> slots = new ArrayList<>(Arrays.asList(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34));
        public ItemConfig entry = new ItemConfig(0, "PAPER", "&#FF4800&lᴏᴘᴇʀᴀᴄᴊᴀ &8• &f%type%",
            "&f", "&8⁎ &fᴘʀᴏᴅᴜᴋᴛ: &e%product%", "&8⁎ &fᴋɪᴇᴅʏ: &e%date%", "&8⁎ &fᴋᴡᴏᴛᴀ: &a%amount% ᴡᴘʟɴ",
            "&8⁎ &fꜱᴀʟᴅᴏ ᴘᴏ: &a%balance% ᴡᴘʟɴ", "&8⁎ &fᴡʏᴋᴏɴᴀʟ: &f%actor%", "&8⁎ &fꜱᴛᴀᴛᴜꜱ: &e%status%", "&8⁎ &fɪᴅ: &7%id%", "&f");
        public ItemConfig previous = new ItemConfig(39, "ARROW", "&e&lᴘᴏᴘʀᴢᴇᴅɴɪᴀ ꜱᴛʀᴏɴᴀ");
        public ItemConfig next = new ItemConfig(41, "ARROW", "&e&lɴᴀꜱᴛᴇᴘɴᴀ ꜱᴛʀᴏɴᴀ");
        public ItemConfig empty = new ItemConfig(22, "BOOK", "&7ʙʀᴀᴋ ʜɪꜱᴛᴏʀɪɪ", "&8⁎ &fᴛᴇɴ ɢʀᴀᴄᴢ ɴɪᴇ ᴍᴀ ᴊᴇꜱᴢᴄᴢᴇ ᴏᴘᴇʀᴀᴄᴊɪ");
    }

    public static class Broadcast extends OkaeriConfig {
        public String title = "&#FFF800&lᴅ&#FDF71C&lᴏ&#FBF638&lʟ&#FAF554&lᴀ&#F8F470&lᴅ&#F6F38C&lᴏ&#F8F470&lᴡ&#FAF554&lᴀ&#FBF638&lɴ&#FDF71C&lɪ&#FFF800&lᴇ";
        public String subtitle = "&fɢʀᴀᴄᴢ %player% ᴅᴏʟᴀᴅᴏᴡᴀʟ %topup_amount% &e☺ &#FFCF00&lɢ&#FFE400&lᴢ&#FFF800&l!&#FFE400&l! &e☺";
        public int fadeIn = 10;
        public int stay = 70;
        public int fadeOut = 20;
        public String sound = "minecraft:custom.itemshop";
        public float volume = 2.0f;
        public float pitch = 1.0f;
    }

    public static class Messages extends OkaeriConfig {
        public String prefix = "&#FF4040&lʜ&#FF5050&lᴏ&#FF6060&lʟ&#FF5050&lʟ&#FF4040&lʏ &8» &r";
        public String usage = "&c☺Poprawne użycie: &4%usage%";
        public String noPermission = "&cBrak dostępu. &7Nie masz wymaganych uprawnień.";
        public String playerOnly = "&7Tę komendę może wykonać &fgracz&7.";
        public String unknownPlayer = "&7Nie znaleziono konta &f%player%&7. Gracz musi wcześniej wejść na serwer.";
        public String invalidAmount = "&7Podaj &fpoprawną kwotę &7z maksymalnie dwoma miejscami po przecinku.";
        public String insufficient = "&cZa mało środków &8• &7koszt &f%holly_cost% WPLN";
        public String changed = "&7Portfel &f%player% &8• &7saldo &#38BDF8%hollywallet_wpln% WPLN";
        public String rewardReceived = "&#68FAA1+%amount% WPLN &8• &7Codzienna nagroda odebrana.";
        public String rewardCooldown = "&7Kolejny odbiór za &#FFCF00%time%&7.";
        public String rewardDisabled = "&7Codzienna nagroda jest obecnie &cwyłączona&7.";
        public String purchased = "&#68FAA1Zakup udany &8• &r%product% &8• &f%holly_cost% WPLN";
        public String unavailable = "&7Produkt jest obecnie &cniedostępny&7.";
        public String purchaseReview = "&#FFCF00Dostawa wymaga weryfikacji &8• &7ID &f%id%";
        public String busy = "&7Trwa zapisywanie operacji. &fPoczekaj chwilę&7.";
        public String error = "&cOperacja przerwana. &7Spróbuj ponownie lub skontaktuj się z administracją.";
        public String reloaded = "&#68FAA1Konfiguracja przeładowana. &7Nowe ustawienia menu są aktywne.";
    }

    public static class FeedbackSounds extends OkaeriConfig {
        public boolean enabled = true;
        public SoundCue success = new SoundCue("minecraft:entity.villager.yes", 1.0f, 1.0f);
        public SoundCue error = new SoundCue("minecraft:entity.villager.no", 1.0f, 1.0f);
    }

    public static class SoundCue extends OkaeriConfig {
        public String sound = "";
        public float volume = 1.0f;
        public float pitch = 1.0f;
        public SoundCue() {}
        public SoundCue(String sound, float volume, float pitch) {
            this.sound = sound; this.volume = volume; this.pitch = pitch;
        }
    }

    private static Map<String, List<Integer>> decorations() {
        Map<String, List<Integer>> map = new LinkedHashMap<>();
        map.put("CYAN_STAINED_GLASS_PANE", Arrays.asList(1,9,7,17,52,44,36,46));
        map.put("WHITE_STAINED_GLASS_PANE", Arrays.asList(2,6,18,26,47,51));
        map.put("LIGHT_BLUE_STAINED_GLASS_PANE", Arrays.asList(0,8,45,53));
        return map;
    }

    private static Map<String, Category> categories() {
        Map<String, Category> map = new LinkedHashMap<>();
        map.put("rangi", new Category());
        return map;
    }

    private static Map<String, Integer> backgroundModels() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("CYAN_STAINED_GLASS_PANE", -1);
        map.put("WHITE_STAINED_GLASS_PANE", -1);
        map.put("LIGHT_BLUE_STAINED_GLASS_PANE", -1);
        return map;
    }

    private static Map<String, Product> products() {
        Map<String, Product> map = new LinkedHashMap<>();
        map.put("vip", new Product());
        return map;
    }
}
