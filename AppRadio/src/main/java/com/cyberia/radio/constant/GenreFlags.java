package com.cyberia.radio.constant;

public interface GenreFlags
{
    String CAT_FAVS = "Favorites";
    String CAT_TREND = "Trending";
    String CAT_MUSIC = "Music";
    String CAT_TALK = "Talk";
    String CAT_BYCOUNTRY = "By Country";
    String CAT_BYLANG = "By Language";
    String CAT_RECENT = "History";
    String CAT_SEARCH = "Search";
    String CAT_SHARE = "Shared";

    int FAVS = 0;
    int TREND = 1;
    int MUSIC = 2;
    int SINGLE_STATION = 3;
    int COUNTRY = 4;
    int LANGUAGE = 5;
    int RECENT = 6;
    int SEARCH = 7;
    int STATIONS = 8;


    String[] RadioCategories = {
                    CAT_FAVS,
                    CAT_TREND,
                    CAT_MUSIC,
                    CAT_TALK,
                    CAT_BYCOUNTRY,
                    CAT_BYLANG,
                    CAT_RECENT,
                    CAT_SEARCH };

//    enum RadioCats
//    {
//        CAT_FAVS("Favorites"),
//        CAT_TREND("Trending"),
//        CAT_MUSIC("Music"),
//        CAT_TALK("Talk"),
//        CAT_BYCOUNTRY("By Country"),
//        CAT_BYLANG("By Language"),
//        CAT_RECENT("History"),
//        CAT_SEARCH("Search"),
//        CAT_SHARE("Shared");
//
//
//        public String item;
//
//        RadioCats(String item)
//        {
//            this.item = item;
//        }
//
//        public static String[] CastToArray()
//        {
//            RadioCats[] cats = RadioCats.values();
//            List<String> list = new ArrayList<>();
//
//            for (RadioCats element : cats)
//            {
//                list.add(element.item);
//            }
//
//            return list.toArray(new String[list.size()]);
//        }
//
//        public static String[] toArray()
//        {
//            RadioCats[] cats = RadioCats.values();
//            String[] stringCats = new String[8];
//
//            for (int i = 0; i < cats.length - 1; i++)
//            {
//                stringCats[i] = cats[i].item;
//            }
//
//            return stringCats;
//        }
//    }
}

