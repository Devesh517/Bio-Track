package projecthealth;

import java.util.Scanner;

public class health {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            while (true) { // Outer loop for main menu
                // Language Selection
                System.out.println("Choose your language / अपनी भाषा चुनें:");
                System.out.println("1. English");
                System.out.println("2. हिंदी");
                System.out.println("3. தமிழ்");
                System.out.println("4. తెలుగు");
                System.out.println("5. বাংলা");
                System.out.println("6. Exit / बाहर निकलें");
                System.out.print("Enter your choice / अपना विकल्प दर्ज करें: ");
                int languageChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (languageChoice == 6) { // Exit option
                    System.out.println("Exiting...");
                    break;
                }

                String lang = switch (languageChoice) {
                    case 1 -> "english";
                    case 2 -> "hindi";
                    case 3 -> "tamil";
                    case 4 -> "telugu";
                    case 5 -> "bengali";
                    default -> {
                        System.out.println("Invalid choice. Defaulting to English.");
                        yield "english";
                    }
                };

                while (true) { // Inner loop for specific language menu
                    // Display menu options
                    switch (lang) {
                        case "english" -> {
                            System.out.println("\n1. Insert Records & Analyze Health Data");
                            System.out.println("2. View Stored Health Data");
                            System.out.println("3. Back to Main Menu");
                            System.out.println("4. Exit");
                            System.out.print("Enter your choice: ");
                        }
                        case "hindi" -> {
                            System.out.println("\n1. रिकॉर्ड दर्ज करें और स्वास्थ्य डेटा का विश्लेषण करें");
                            System.out.println("2. संग्रहीत स्वास्थ्य डेटा देखें");
                            System.out.println("3. मुख्य मेनू पर वापस जाएं");
                            System.out.println("4. बाहर निकलें");
                            System.out.print("अपना विकल्प दर्ज करें: ");
                        }
                        case "tamil" -> {
                            System.out.println("\n1. பதிவு செய்க மற்றும் ஆரோக்கிய தரவுகளை பகுப்பாய்வு செய்யவும்");
                            System.out.println("2. சேமிக்கப்பட்ட ஆரோக்கிய தரவுகளை பார்க்கவும்");
                            System.out.println("3. மெயின் மெனு செல்ல");
                            System.out.println("4. வெளியேறு");
                            System.out.print("உங்கள் தேர்வை உள்ளிடவும்: ");
                        }
                        case "telugu" -> {
                            System.out.println("\n1. రికార్డులను నమోదు చేయండి మరియు ఆరోగ్య డేటాను విశ్లేషించండి");
                            System.out.println("2. నిల్వ చేసిన ఆరోగ్య డేటాను చూడండి");
                            System.out.println("3. ప్రధాన మెనూకు వెళ్ళండి");
                            System.out.println("4. బయటపడండి");
                            System.out.print("మీ ఎంపికను నమోదు చేయండి: ");
                        }
                        case "bengali" -> {
                            System.out.println("\n1. রেকর্ড সন্নিবেশ করুন এবং স্বাস্থ্য ডেটা বিশ্লেষণ করুন");
                            System.out.println("2. সংরক্ষিত স্বাস্থ্য ডেটা দেখুন");
                            System.out.println("3. মূল মেনুতে ফিরে যান");
                            System.out.println("4. বাহিরে যান");
                            System.out.print("আপনার পছন্দ লিখুন: ");
                        }
                        default -> {}
                    }

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    switch (choice) {
                        case 1 -> {
                           switch (lang) {
                        case "english" -> System.out.println("Enter your name: ");
                        case "hindi" -> System.out.println("अपना नाम दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் பெயர் உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ పేరు నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার নাম লিখুন: ");
                        default -> {
                        }
                    }
String name = scanner.nextLine();
                    switch (lang) {
                        case "english" -> System.out.println("Enter your age: ");
                        case "hindi" -> System.out.println("अपनी आयु दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் வயது உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ వయస్సు నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার বয়স লিখুন: ");
                        default -> {
                        }
                    }
int age = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                    switch (lang) {
                        case "english" -> System.out.println("Enter your gender (Male/Female/Other): ");
                        case "hindi" -> System.out.println("अपना लिंग दर्ज करें (पुरुष/महिला/अन्य): ");
                        case "tamil" -> System.out.println("உங்கள் பாலினத்தை உள்ளிடவும் (ஆண்/பெண்/மற்றவை): ");
                        case "telugu" -> System.out.println("మీ లింగాన్ని నమోదు చేయండి (పురుషుడు/స్త్రీ/ఇతర): ");
                        case "bengali" -> System.out.println("আপনার লিঙ্গ লিখুন (পুরুষ/মহিলা/অন্যান্য): ");
                        default -> {
                        }
                    }
                            String gender = scanner.nextLine();
                            // Insert user data and get user_id
                            int userId = datamanager.insertUser(name, age, gender);
                            if (userId == -1) {
                                System.out.println("Error saving user details. Exiting...");
                                continue;
                            }       switch (lang) {
                        case "english" -> System.out.println("Your User ID is: " + userId);
                        case "hindi" -> System.out.println("आपका उपयोगकर्ता आईडी है: " + userId);
                        case "tamil" -> System.out.println("உங்கள் பயனர் ஐடி: " + userId);
                        case "telugu" -> System.out.println("మీ వినియోగదారు ఐడీ: " + userId);
                        case "bengali" -> System.out.println("আপনার ব্যবহারকারী আইডি হল: " + userId);
                        default -> {
                        }
                    }
                    switch (lang) {
                        case "english" -> System.out.println("\nEnter your weight (in kg): ");
                        case "hindi" -> System.out.println("\nअपना वजन (किग्रा में) दर्ज करें: ");
                        case "tamil" -> System.out.println("\nஉங்கள் எடை (கிலோகிராம்) உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("\nమీ బరువు (కిలోలలో) నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("\nআপনার ওজন (কেজিতে) লিখুন: ");
                        default -> {
                        }
                    }
double weight = scanner.nextDouble();
              // Consume newline
                    // ... Inside the lang switch case for user input section:
switch (lang) {
    case "english" -> {
        System.out.println("Enter your height:");
        System.out.println("1. In feet and inches");
        System.out.println("2. In meters");
        System.out.print("Choose an option: ");
    }
    case "hindi" -> {
        System.out.println("अपनी ऊँचाई दर्ज करें:");
        System.out.println("1. फीट और इंच में");
        System.out.println("2. मीटर में");
        System.out.print("एक विकल्प चुनें: ");
    }
    case "tamil" -> {
        System.out.println("உங்கள் உயரத்தை உள்ளிடவும்:");
        System.out.println("1. அடிகள் மற்றும் அங்குலங்களில்");
        System.out.println("2. மீட்டர்களில்");
        System.out.print("ஒரு விருப்பத்தை தேர்ந்தெடுக்கவும்: ");
    }
    case "telugu" -> {
        System.out.println("మీ ఎత్తు నమోదు చేయండి:");
        System.out.println("1. అడుగులు మరియు ఇంచ్‌లలో");
        System.out.println("2. మీటర్లలో");
        System.out.print("ఒక ఎంపికను ఎంచుకోండి: ");
    }
    case "bengali" -> {
        System.out.println("আপনার উচ্চতা লিখুন:");
        System.out.println("1. ফুট এবং ইঞ্চিতে");
        System.out.println("2. মিটারে");
        System.out.print("একটি বিকল্প চয়ন করুন: ");
    }
    default -> {
    }
}
int heightOption = scanner.nextInt();
scanner.nextLine(); // Consume newline

double height;
if (heightOption == 1) {
    // Input height in feet and inches
    switch (lang) {
        case "english" -> System.out.print("Enter height in feet: ");
        case "hindi" -> System.out.print("फीट में ऊँचाई दर्ज करें: ");
        case "tamil" -> System.out.print("அடிகளில் உயரத்தை உள்ளிடவும்: ");
        case "telugu" -> System.out.print("అడుగులలో ఎత్తు నమోదు చేయండి: ");
        case "bengali" -> System.out.print("ফুটে উচ্চতা লিখুন: ");
        default -> {
        }
    }
    int feet = scanner.nextInt();

    switch (lang) {
        case "english" -> System.out.print("Enter height in inches: ");
        case "hindi" -> System.out.print("इंच में ऊँचाई दर्ज करें: ");
        case "tamil" -> System.out.print("அங்குலங்களில் உயரத்தை உள்ளிடவும்: ");
        case "telugu" -> System.out.print("ఇంచులలో ఎత్తు నమోదు చేయండి: ");
        case "bengali" -> System.out.print("ইঞ্চিতে উচ্চতা লিখুন: ");
        default -> {
        }
    }
    int inches = scanner.nextInt();
    scanner.nextLine(); // Consume newline
    height = (feet * 12 + inches) * 0.0254; // Convert to meters
} else {
    // Input height in meters
    switch (lang) {
        case "english" -> System.out.print("Enter your height (in meters): ");
        case "hindi" -> System.out.print("अपनी ऊँचाई (मीटर में) दर्ज करें: ");
        case "tamil" -> System.out.print("உங்கள் உயரத்தை (மீட்டரில்) உள்ளிடவும்: ");
        case "telugu" -> System.out.print("మీ ఎత్తు (మీటర్లలో) నమోదు చేయండి: ");
        case "bengali" -> System.out.print("আপনার উচ্চতা (মিটারে) লিখুন: ");
        default -> {
        }
    }
    height = scanner.nextDouble();
    scanner.nextLine(); // Consume newline
}

                    switch (lang) {
                        case "english" -> System.out.println("Enter your body temperature (in °C): ");
                        case "hindi" -> System.out.println("अपना शरीर का तापमान (°C में) दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் உடல் வெப்பநிலையை (°C) உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ శరీర ఉష్ణోగ్రత (°C) నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার শরীরের তাপমাত্রা (°C) লিখুন: ");
                        default -> {
                        }
                    }
double temperature = scanner.nextDouble();
                            scanner.nextLine(); // Consume newline
                    switch (lang) {
                        case "english" -> System.out.println("Enter your blood pressure (in mmHg): ");
                        case "hindi" -> System.out.println("अपना रक्तचाप (mmHg में) दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் இரத்த அழுத்தத்தை (mmHg) உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ రక్తపోటు (mmHg) నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার রক্তচাপ (mmHg) লিখুন: ");
                        default -> {
                        }
                    }
int bloodPressure = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                    switch (lang) {
                        case "english" -> System.out.println("Enter your heart rate (in bpm): ");
                        case "hindi" -> System.out.println("अपना हृदय दर (bpm में) दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் இதய துடிப்பை (bpm) உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ హృదయ స్పందన రేటు (bpm) నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার হার্ট রেট (bpm) লিখুন: ");
                        default -> {
                        }
                    }
int heartRate = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                    switch (lang) {
                        case "english" -> System.out.println("Enter your sugar level (in mg/dL): ");
                        case "hindi" -> System.out.println("अपना शुगर स्तर (mg/dL में) दर्ज करें: ");
                        case "tamil" -> System.out.println("உங்கள் சர்க்கரை நிலையை (mg/dL) உள்ளிடவும்: ");
                        case "telugu" -> System.out.println("మీ చక్కెర స్థాయి (mg/dL) నమోదు చేయండి: ");
                        case "bengali" -> System.out.println("আপনার চিনি স্তর (mg/dL) লিখুন: ");
                        default -> {
                        }
                    }
int sugarLevel = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            double bmi = healthdata.getcalculateBMI(weight, height);
                            String analysis = analyzer.analyzeData(weight, height, bmi, temperature, bloodPressure, heartRate, sugarLevel);
                            // Display inputted data and analysis
                            System.out.println("\nInputted Data:");
                            System.out.println("Weight: " + weight + " kg");
                            System.out.println("Height: " + height + " m");
                            System.out.println("BMI: " + bmi);
                            System.out.println("Body Temperature: " + temperature + " °C");
                            System.out.println("Blood Pressure: " + bloodPressure + " mmHg");
                            System.out.println("Heart Rate: " + heartRate + " bpm");
                            System.out.println("Sugar Level: " + sugarLevel + " mg/dL");
                            System.out.println("\nAnalysis:");
                            System.out.println(analysis);
                            // Insert health data for the user
                            datamanager.insertHealthData(userId, weight, height, bmi, temperature, bloodPressure, heartRate, sugarLevel);
                       
                        }
                        case 2 -> {
                            switch (lang) {
                        case "english" -> System.out.print("Enter your User ID: ");
                        case "hindi" -> System.out.print("अपना उपयोगकर्ता आईडी दर्ज करें: ");
                        case "tamil" -> System.out.print("உங்கள் பயனர் ஐடியை உள்ளிடவும்: ");
                        case "telugu" -> System.out.print("మీ వినియోగదారు ఐడీని నమోదు చేయండి: ");
                        case "bengali" -> System.out.print("আপনার ব্যবহারকারী আইডি লিখুন: ");
                        default -> {
                        }
                    }
int userId = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            // Fetch data for the specific User ID
                            String userData = datamanager.fetchDataById(userId);
                            if (userData.isEmpty()) {
                        switch (lang) {
                            case "english" -> System.out.println("No data found for User ID: " + userId);
                            case "hindi" -> System.out.println("उपयोगकर्ता आईडी " + userId + " के लिए कोई डेटा नहीं मिला।");
                            case "tamil" -> System.out.println("பயனர் ஐடி " + userId + "க்கு எந்த தரவும் இல்லை.");
                            case "telugu" -> System.out.println("వినియోగదారు ఐడీ " + userId + " కోసం డేటా దొరకలేదు.");
                            case "bengali" -> System.out.println("ব্যবহারকারী আইডি " + userId + " এর জন্য কোনও তথ্য পাওয়া যায়নি।");
                            default -> {
                            }
                        }
                                
                            } else {
                        switch (lang) {
                            case "english" -> System.out.println("Stored Health Data for User ID " + userId + ":");
                            case "hindi" -> System.out.println("उपयोगकर्ता आईडी " + userId + " के लिए संग्रहीत स्वास्थ्य डेटा:");
                            case "tamil" -> System.out.println("பயனர் ஐடி " + userId + "க்கு சேமிக்கப்பட்ட ஆரோக்கிய தரவுகள்:");
                            case "telugu" -> System.out.println("వినియోగదారు ఐడీ " + userId + " కోసం నిల్వచేసిన ఆరోగ్య డేటా:");
                            case "bengali" -> System.out.println("ব্যবহারকারী আইডি " + userId + " এর জন্য সংরক্ষিত স্বাস্থ্য তথ্য:");
                            default -> {
                            }
                        }
                                
                                
                                System.out.println(userData);
                            }
                        }
                        case 3 -> {
                            // Back to main menu
                            System.out.println("Returning to main menu...");
                            System.out.print("\033[H\033[2J");
                            System.out.flush();
                            break;
                        }
                        case 4 -> {
                            switch (lang) {
                        case "english" -> System.out.println("Exiting...");
                        case "hindi" -> System.out.println("बाहर जा रहे हैं...");
                        case "tamil" -> System.out.println("வெளியேறுகிறேன்...");
                        case "telugu" -> System.out.println("బయటపోతున్నాను...");
                        case "bengali" -> System.out.println("বেরিয়ে যাচ্ছি...");
                        default -> {
                        }
                    }
              return;
                        }
                        default -> {
                            // Invalid option
                            switch (lang) {
                                case "english" -> System.out.println("Invalid choice. Please try again.");
                                case "hindi" -> System.out.println("अमान्य विकल्प। कृपया पुनः प्रयास करें।");
                                case "tamil" -> System.out.println("செல்லாத தேர்வு. தயவுசெய்து மீண்டும் முயற்சிக்கவும்.");
                                case "telugu" -> System.out.println("తప్పుదారిన ఎంపిక. దయచేసి మళ్లీ ప్రయత్నించండి.");
                                case "bengali" -> System.out.println("অবৈধ পছন্দ। দয়া করে আবার চেষ্টা করুন।");
                                default -> {}
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
