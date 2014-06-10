/*
 * This file is part of the Anthony Lomax Java Library.
 *
 * Copyright (C) 2000 Anthony Lomax <anthony@alomax.net www.alomax.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.alomax.text;

/**
 * A class for multi-language text
 *
 * All of the methods of this class are static.
 */
public class GeneralText extends LocalizedText {

    // default en_US text
    public static String YOU_ARE_USING_JAVA_VERSION = " ";
    public static String NO_WARRANTY = " ";
    public static String COPYRIGHT = " ";
    public static String CONCEIVED_AND_DEVELOPED_BY = "";
    public static String TRANSLATIONS_PROVIDED_BY = "";
    public static String SUPPORTED_BY = "";
    public static String OPERATION_SUCCEEDED = " ";
    public static String FILE_COULD_NOT_BE_SAVED = " ";
    public static String ENABLE = " ";
    public static String en_US = " ";
    public static String fr_FR = " ";
    public static String it_IT = " ";
    public static String pt_BR = " ";
    public static String tr_TR = " ";
    public static String zh_TW = " ";

    static {
        initText();
    }

    /**
     * Sets text to default
     */
    protected static void initText() {


        YOU_ARE_USING_JAVA_VERSION = "You are using Java version";

        NO_WARRANTY = " comes with ABSOLUTELY NO WARRANTY";

        COPYRIGHT =
                "This program is part of the Anthony Lomax Java Library." + ENDLINE
                + ENDLINE
                + "This program is free software; you can redistribute it and/or modify" + ENDLINE
                + "it under the terms of the GNU General Public License as published by" + ENDLINE
                + "the Free Software Foundation; either version 2 of the License, or" + ENDLINE
                + "(at your option) any later version." + ENDLINE
                + ENDLINE
                + "This program is distributed in the hope that it will be useful," + ENDLINE
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of" + ENDLINE
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" + ENDLINE
                + "GNU General Public License for more details." + ENDLINE
                + ENDLINE
                + "You should have received a copy of the GNU General Public License" + ENDLINE
                + "along with this program; if not, write to the Free Software" + ENDLINE
                + "Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.";

        CONCEIVED_AND_DEVELOPED_BY = "This software was conceived and developed by:";
        TRANSLATIONS_PROVIDED_BY = "Thanks to the following people for providing language translations:";
        SUPPORTED_BY = "The development of the this software is supported by:";


        // general messages

        OPERATION_SUCCEEDED = "Operation succeeded";
        FILE_COULD_NOT_BE_SAVED = "The file could not be saved, as it was not possible to write to: ";

        ENABLE = "Enable";

        en_US = "English";
        fr_FR = "French";
        it_IT = "Italian";
        pt_BR = "Portuguese";
        tr_TR = "Turkish";
        zh_TW = "Chinese";

    }

    /**
     * Sets locale and text
     */
    public static void setLocale(String locName) {

        initText();

        LocalizedText.setLocale(locName);

        //System.out.println("localeName " + localeName);

        // French, France

        if (localeName.toLowerCase().startsWith("fr")) {

            YOU_ARE_USING_JAVA_VERSION = "Vous utiliser la version Java";

            NO_WARRANTY = " n'est accompagn" + e_a + " d'ABSOLUMENT AUCUNE GARANTIE";

            COPYRIGHT =
                    "Ce programme est une partie de la Biblioth" + e_g + "que de Logiciel Java Anthony Lomax." + ENDLINE
                    + ENDLINE
                    + "Ce programme est un logiciel libre ; vous pouvez le redistribuer et/ou le modifier" + ENDLINE
                    + "au titre des clauses de la Licence Publique G" + e_a + "n" + e_a + "rale GNU, telle que publi" + e_a + "e par" + ENDLINE
                    + "la Free Software Foundation ; soit la version 2 de la Licence, ou" + ENDLINE
                    + "(" + a_g + " votre discr" + e_a + "tion) une version ult" + e_a + "rieure quelconque." + ENDLINE
                    + ENDLINE
                    + "Ce programme est distribu" + e_a + " dans l'espoir qu'il sera utile," + ENDLINE
                    + "mais SANS AUCUNE GARANTIE ; sans m" + e_c + "me une garantie implicite de" + ENDLINE
                    + "COMMERCIABILITE ou DE CONFORMITE A UNE UTILISATION PARTICULIERE. Voir" + ENDLINE
                    + "la Licence Publique G" + e_a + "n" + e_a + "rale GNU pour plus de d" + e_a + "tails." + ENDLINE
                    + ENDLINE
                    + "Vous devriez avoir re" + c_5 + "u un exemplaire de la Licence Publique G" + e_a + "n" + e_a + "rale GNU" + ENDLINE
                    + "avec ce programme ; si ce n'est pas le cas, " + e_a + "crivez " + a_g + " la Free Software" + ENDLINE
                    + "Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.";

            CONCEIVED_AND_DEVELOPED_BY = "Ce logiciel a " + e_a + "t" + e_a + " con" + c_5 + "u et d" + e_a + "velopp" + e_a + " par:";
            TRANSLATIONS_PROVIDED_BY = "Merci aux personnes suivantes de avoir founis des traductions des langues:";
            SUPPORTED_BY = "Le d" + e_a + "veloppement du ce logiciel est soutenu par:";


            // general text

            OPERATION_SUCCEEDED = "R" + e_a + "ussite de l’op" + e_a + "ration";
            FILE_COULD_NOT_BE_SAVED = "Le fichier n'" + e_a + "tait pas enregistrer, il n'" + e_a + "tait pas possible d'" + e_a + "crire " + a_g + ": ";

            ENABLE = "Activer";

            en_US = "Anglais";
            fr_FR = "Fran" + c_5 + "ais";
            it_IT = "Italien";
            pt_BR = "Portugais";
            tr_TR = "Turc";
            zh_TW = "Chinois";

        } // Italian, Italy
        else if (localeName.toLowerCase().startsWith("it")) {

            YOU_ARE_USING_JAVA_VERSION = "Versione da Java";

            NO_WARRANTY = " " + e_g + " distribuito SENZA ALCUNA GARANZIA";

            COPYRIGHT =
                    "Questo programma " + e_g + " una parta della Biblioteca del programma Java Anthony Lomax." + ENDLINE
                    + ENDLINE
                    + "Questo pragramma " + e_g + " libero; potete distribuirlo e/o modificarlo" + ENDLINE
                    + "rispettando le clausole della Licenza Pubblica Generica GNU, cos " + i_g + " come pubblicata" + ENDLINE
                    + "dalla Free Software Foundation ; oppure la versione 2 della licenza, o" + ENDLINE
                    + "(a vostra discrezione) una qualsiasi versione ulteriore." + ENDLINE
                    + ENDLINE
                    + "Questo programma " + e_g + " distribuito nella speranzo che sar" + a_g + " utile," + ENDLINE
                    + "ma SENZA ALCUNA GARANZIA ; senza neanche la garanzia implicita di" + ENDLINE
                    + "COMMERCIABILITA ou DI CONFORMITA A UN'UTILIZZAZIONE PARTICOLARE. Vedi" + ENDLINE
                    + "la Licenza Pubblica Generica GNU per ulteriori dettagli e informazioni." + ENDLINE
                    + ENDLINE
                    + "Dovreste aver ricevuto un essemplare della Licenza Pubblica Generica GNU" + ENDLINE
                    + "assieme a questo programma ; in case contratrio, scrivete alla Free Software" + ENDLINE
                    + "Foundation Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.";

            CONCEIVED_AND_DEVELOPED_BY = "Ce logiciel a " + e_a + "t" + e_a + " con" + c_5 + "u et d" + e_a + "velopp" + e_a + " par:";
            TRANSLATIONS_PROVIDED_BY = "Ringraziamenti alla seguente gente per avere fornitura delle traduzioni di linguaggio:";
            SUPPORTED_BY = "Le d" + e_a + "veloppement du ce logiciel est soutenu par:";


            // general text

            OPERATION_SUCCEEDED = "R" + e_a + "ussite de l’op" + e_a + "ration";
            FILE_COULD_NOT_BE_SAVED = "Le fichier n'" + e_a + "tait pas enregistrer, il n'" + e_a + "tait pas possible d'" + e_a + "crire " + a_g + ": ";

            ENABLE = "Attiva";

            en_US = "Inglese";
            fr_FR = "Francese";
            it_IT = "Italiano";
            pt_BR = "Portoghese";
            tr_TR = "Turko";
            zh_TW = "Cinese";


        } // portugues, Brasil
        else if (localeName.toLowerCase().startsWith("pt")) {

            YOU_ARE_USING_JAVA_VERSION = "Voc" + e_c + " est" + a_a + " usando Java versao";

            NO_WARRANTY = " vem absolutamente sem garantia.";

            COPYRIGHT =
                    "Esse programa " + e_a + " parte da Anthony Lomax Java Library." + ENDLINE
                    + ENDLINE
                    + "Esse programa " + e_a + " um software livre; voc" + e_c + " pode redistribui-lo e/ou modific" + a_a + "-lo" + ENDLINE
                    + "sob os termos da GNU GPL publicada pela FSF " + ENDLINE
                    + "sob a vers" + a_t + "o 2 ou superior (sob sua opiniao) da licenca." + ENDLINE
                    + ENDLINE
                    + "Este programa " + e_a + " distribu" + i_a + "do na esperan" + c_5 + "a de ser " + u_a + "til," + ENDLINE
                    + "mas NAO OFERECE QUALQUER GARANTIA; nem mesmo garantias de " + ENDLINE
                    + "COMERCIALIZACAO ou AJUSTES PARA FINS PARTICULARES. " + ENDLINE
                    + "Veja a GNU-GPL para maiores detalhes" + ENDLINE
                    + ENDLINE
                    + "Voc" + e_c + " deve receber uma c" + o_a + "pia da GPL, sen" + a_t + "o escreva para " + ENDLINE
                    + "Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA." + ENDLINE
                    + "";

            CONCEIVED_AND_DEVELOPED_BY = "Este software foi concebido e desenvolvido por:";
            TRANSLATIONS_PROVIDED_BY = "Thanks to the following people for providing language translations:";
            SUPPORTED_BY = "O desenvolvimento do software " + e_a + " suportado por:";


            // general messages

            OPERATION_SUCCEEDED = "Opera" + c_5 + "" + a_t + "o Conclu" + i_a + "da";
            FILE_COULD_NOT_BE_SAVED = "O arquivo n" + a_t + "o pode ser salvo, n" + a_t + "o foi poss" + i_a + "vel escrever em: ";

            ENABLE = "Habilitado";

            en_US = "Ingles";
            fr_FR = "Frances";
            it_IT = "Italiano";
            pt_BR = "Portugues";
            tr_TR = "Turko";
            zh_TW = "Chinês";

        } // Turkish
        else if (localeName.toLowerCase().startsWith("tr")) {


            YOU_ARE_USING_JAVA_VERSION = "Java s" + u_s + "r" + u_s + "m" + u_s + " kullan" + i_s + "yorsunuz";

            NO_WARRANTY = " Garanti kapsam" + i_s + " d" + i_s + "" + s_s + "" + i_s + "ndad" + i_s + "r";

            COPYRIGHT =
                    " Anthony Lomaxí" + i_s + "n JAVA k" + u_s + "t" + u_s + "phanesine aittir." + ENDLINE
                    + ENDLINE
                    + "Bu program " + u_s + "cretsiz bir yaz" + i_s + "l" + i_s + "md" + i_s + "r; " + ENDLINE
                    + " " + u_s + "cretsiz yaz" + i_s + "l" + i_s + "m birli" + g_s + "i  (SF) taraf" + i_s + "ndan yay"
                    + i_s + "nlanm" + i_s + "" + s_s + " olan GNU (Genel Halk Lisans" + i_s + "na g" + o_s + "re da"
                    + g_s + "" + i_s + "tabilir veya " + u_s + "zerinde de" + g_s + "i" + s_s + "iklik yapabilirsiniz." + ENDLINE
                    + "Bu program faydal" + i_s + " olabilece" + g_s + "i inanc" + i_s + "na g" + o_s + "re da" + g_s
                    + "" + i_s + "t" + i_s + "lm" + i_s + "" + s_s + "t" + i_s + "r" + ENDLINE + "ìfakat herhangi bir garantisi yoktur" + ENDLINE
                    + "detay bilgi i" + c_5 + "in GNU General Public License. a muracaat ediniz" + ENDLINE
                    + "You should have received a copy of the GNU General Public License" + ENDLINE
                    + "along with this program; if not, write to the Free Software" + ENDLINE
                    + "Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.";

            CONCEIVED_AND_DEVELOPED_BY = "Bu yaz" + i_s + "l" + i_s + "m taraf" + i_s + "ndan tasarlan" + i_s + "p geli" + s_s + "tirilmi" + s_s + "tir";
            TRANSLATIONS_PROVIDED_BY = "Lisan terc" + u_s + "mesi yapan ki" + s_s + "ilere te" + s_s + "ekk" + u_s + "r ederim";
            SUPPORTED_BY = "Bu yaz" + i_s + "l" + i_s + "m taraf" + i_s + "ndan desteklenmi" + s_s + "tir:";


            // general messages

            OPERATION_SUCCEEDED = "" + i_b + "" + s_s + "lem ba" + s_s + "ar" + i_s + "ld" + i_s + "";
            FILE_COULD_NOT_BE_SAVED = "" + u_s + "zerine yazmak m" + u_s + "mk" + u_s + "n olmad" + i_s + "" + g_s + "" + i_s + "ndan bu dosya kaydedilememi" + s_s + "tir ";

            ENABLE = "Olanakl" + i_s + "";

            en_US = "›ngilizce";
            fr_FR = "Frans" + i_s + "zca";
            it_IT = i_b + "talyanca";
            pt_BR = "Portekizce";
            tr_TR = "T" + u_s + "rk" + c_5 + "e";
            zh_TW = "Çince";



        } // Chinese (zh_TW)
        else if (localeName.toLowerCase().startsWith("zh")) {


            YOU_ARE_USING_JAVA_VERSION = "你所使用的Java版本程式";

            NO_WARRANTY = " 不負任何擔保責任";

            COPYRIGHT =
                    "這個程式是 Anthony Lomax Java Library 的一部分。" + ENDLINE
                    + ENDLINE
                    + "由於這個程式屬於自由軟體，你可以在接受自由軟體基金會所發表的 GNU" + ENDLINE
                    + "通用公眾授權條款(GPL)的情況下自行發布/修改軟體(不論是第二版" + ENDLINE
                    + "或是後來其他版的條款)。" + ENDLINE
                    + ENDLINE
                    + "發布這個軟體的用意是希望它能對社群有所助益，但是絕對不負任何可能之" + ENDLINE
                    + "損害責任，; " + ENDLINE
                    + "且不含商業行為或" + ENDLINE
                    + "適用於特定用途的保證。其他詳情" + ENDLINE
                    + "請參閱 GNU 通用公眾授權條款" + ENDLINE
                    + ENDLINE
                    + "你應該已隨著此軟體取得GNU通用公眾授權書; " + ENDLINE
                    + "若無，請寫信到自由軟體基金會(Free Software Foundation, Inc.," + ENDLINE
                    + "59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.)";

            CONCEIVED_AND_DEVELOPED_BY = "此軟體的開發與設計由";
            TRANSLATIONS_PROVIDED_BY = "感謝以下人員提供語言翻譯:";
            SUPPORTED_BY = "本軟體開發之資助由:";


            // general messages

            OPERATION_SUCCEEDED = "執行完成";
            FILE_COULD_NOT_BE_SAVED = "該檔案無法存檔，此檔案無法寫入！ ";

            ENABLE = "啟用";

            en_US = "英文";
            fr_FR = "法文";
            it_IT = "義大利文";
            pt_BR = "葡萄牙文";
            tr_TR = "土耳其文";
            zh_TW = "繁體中文 - 台灣";

        } // default English, USA
        else {		// use default en_US
        }

    }
}	// end class SeisGramText

