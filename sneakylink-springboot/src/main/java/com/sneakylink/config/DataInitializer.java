package com.sneakylink.config;

import com.sneakylink.model.Shoe;
import com.sneakylink.repository.ShoeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final ShoeRepository shoeRepository;

    public DataInitializer(ShoeRepository shoeRepository) {
        this.shoeRepository = shoeRepository;
    }

    @Override
    public void run(String... args) {
        if (shoeRepository.count() >= 45) {
            log.info("SneakyLink catalog already initialized with {} footwear models.", shoeRepository.count());
            return;
        }

        List<Shoe> catalog = List.of(
                shoe("SL-001", "AeroPulse Cobalt X", "SneakyLab", "Running", "Cobalt / Obsidian", "Blue", "US 7-12", "7,8,9,10,11,12", "Engineered Mesh", "165.00", 24, "#38BDF8", "#1E3A8A", "Featherweight carbon-plated road runner tuned for daily tempo sessions.", true),
                shoe("SL-002", "Vortex Shadow High", "ApexKicks", "Basketball", "Stealth Black / Ice", "Black", "US 8-13", "8,9,10,11,12,13", "Full-Grain Leather", "195.00", 18, "#60A5FA", "#111827", "High-top court silhouette with dual-density ankle lockdown and herringbone grip.", true),
                shoe("SL-003", "Stratos Knit Low", "Velocity", "Lifestyle", "Midnight / Glacier", "Blue", "US 6-11", "6,7,8,9,10,11", "Primeknit Textile", "140.00", 31, "#3B82F6", "#0F172A", "Breathable sock-fit daily runner with responsive nitrogen-infused midsole.", true),
                shoe("SL-004", "Obsidian Drift Pro", "SneakyLab", "Lifestyle", "Triple Obsidian", "Black", "US 7-13", "7,8,9,10,11,12,13", "Suede & Nubuck", "158.00", 14, "#2563EB", "#090B10", "Tonal matte nubuck upper paired with sculpted geometric outsole.", true),
                shoe("SL-005", "Glacier Trail GTX", "SummitRun", "Trail", "Arctic Blue / Slate", "Blue", "US 8-13", "8,9,10,11,12,13", "Gore-Tex Ripstop", "210.00", 12, "#38BDF8", "#1E293B", "Waterproof alpine trail shoe with aggressive 5mm Vibram-style lugs.", false),
                shoe("SL-006", "CyberCourt 88", "ApexKicks", "Basketball", "Electric Blue / White", "Blue", "US 7-13", "7,8,9,10,11,12,13", "Full-Grain Leather", "175.00", 20, "#2563EB", "#1D4ED8", "Retro 1988 hardwood classic refreshed with Zoom-grade heel cushioning.", true),
                shoe("SL-007", "Phantom Vulcan Skate", "GridLine", "Skate", "Charcoal / Cobalt", "Black", "US 6-12", "6,7,8,9,10,11,12", "Canvas & Suede", "98.00", 40, "#38BDF8", "#18181B", "Reinforced foxing tape and impact-absorbing footbed built for boardfeel.", false),
                shoe("SL-008", "Nova React Runner", "Velocity", "Running", "Sapphire / Silver", "Blue", "US 5-10", "5,6,7,8,9,10", "Engineered Mesh", "152.00", 27, "#60A5FA", "#1E40AF", "Rocker-geometry trainer engineered for effortless marathon mileage.", false),
                shoe("SL-009", "Monolith Suede 990", "NorthCraft", "Lifestyle", "Storm Grey / Navy", "Grey", "US 7-12", "7,8,9,10,11,12", "Suede & Nubuck", "215.00", 9, "#3B82F6", "#334155", "Heritage pigskin suede craftsmanship with dual-compound heel stabilizer.", true),
                shoe("SL-010", "HyperLift Metcon", "IronForm", "Training", "Onyx / Royal", "Black", "US 8-14", "8,9,10,11,12,13,14", "Ballistic Ripstop", "145.00", 22, "#2563EB", "#111827", "Wide flat heel base and rope-wrap midfoot for heavy gym sessions.", false),
                shoe("SL-011", "Polaris Slip-On Knit", "SneakyLab", "Lifestyle", "Ice White / Blue", "White", "US 5-10", "5,6,7,8,9,10", "Recycled Knit", "115.00", 35, "#38BDF8", "#CBD5E1", "Zero-lace eco-knit commuter shoe made from 85% ocean-bound yarn.", false),
                shoe("SL-012", "Nightfall Runner V2", "Velocity", "Running", "Jet Black / Cyan", "Black", "US 7-12", "7,8,9,10,11,12", "Engineered Mesh", "168.00", 19, "#06B6D4", "#0B0F19", "Reflective 3M overlays and plush foam stack built for night road runs.", false),
                shoe("SL-013", "Cobalt Force Low", "ApexKicks", "Lifestyle", "Royal Blue / Black", "Blue", "US 6-12", "6,7,8,9,10,11,12", "Full-Grain Leather", "130.00", 28, "#3B82F6", "#1D4ED8", "Clean cupsole street icon with perforated toe box and crisp contrast stitching.", false),
                shoe("SL-014", "Tundra Peak Mid", "SummitRun", "Trail", "Deep Navy / Ash", "Blue", "US 8-13", "8,9,10,11,12,13", "Gore-Tex Ripstop", "225.00", 8, "#60A5FA", "#172554", "Mid-cut technical hiker shielding ankles from scree and wet alpine crossings.", false),
                shoe("SL-015", "Kinetic Fly 360", "IronForm", "Training", "Graphite / Electric", "Grey", "US 7-12", "7,8,9,10,11,12", "Primeknit Textile", "138.00", 26, "#38BDF8", "#475569", "Multi-directional traction pods designed for agility ladders and HIIT.", false),
                shoe("SL-016", "Onyx Patent Court", "LuxStride", "Luxury", "Gloss Black / Sapphire", "Black", "US 7-12", "7,8,9,10,11,12", "Patent Leather", "285.00", 6, "#2563EB", "#09090B", "Italian-tanned patent leather low-top with hand-burnished midsole edges.", true),
                shoe("SL-017", "Slate Canvas Classic", "GridLine", "Skate", "Washed Slate / White", "Grey", "US 5-10", "5,6,7,8,9,10", "Canvas & Suede", "85.00", 44, "#94A3B8", "#334155", "Timeless low-profile skate sneaker with double-stitched ollie panel.", false),
                shoe("SL-018", "Horizon Boost Max", "Velocity", "Running", "Sky Blue / Obsidian", "Blue", "US 7-13", "7,8,9,10,11,12,13", "Primeknit Textile", "188.00", 16, "#38BDF8", "#0284C7", "Maximum-cushion long-distance cruiser with energy-return bead midsole.", false),
                shoe("SL-019", "Eclipse Mid Retro", "ApexKicks", "Basketball", "Black / Metallic Blue", "Black", "US 8-14", "8,9,10,11,12,13,14", "Full-Grain Leather", "182.00", 15, "#3B82F6", "#111827", "Sculpted midfoot strap and icy translucent outsole for hardwood dominance.", false),
                shoe("SL-020", "Driftwood Suede Lo", "NorthCraft", "Lifestyle", "Sand / Cobalt Accent", "Neutral", "US 6-11", "6,7,8,9,10,11", "Suede & Nubuck", "162.00", 21, "#2563EB", "#78716C", "Soft brushed suede with subtle cobalt heel tab and gum tread.", false),
                shoe("SL-021", "Aether Carbon Elite", "SneakyLab", "Running", "Neon Cyan / Pitch", "Blue", "US 7-12", "7,8,9,10,11,12", "Engineered Mesh", "245.00", 5, "#06B6D4", "#0F172A", "Race-day super-shoe featuring full-length forked carbon fiber propulsion.", true),
                shoe("SL-022", "Steel City Trainer", "IronForm", "Training", "Titanium / Black", "Grey", "US 8-13", "8,9,10,11,12,13", "Ballistic Ripstop", "134.00", 29, "#60A5FA", "#3F3F46", "Abrasion-resistant forefoot and locked-in TPU heel clip.", false),
                shoe("SL-023", "Frostbite Runner", "Velocity", "Running", "Glacier White / Navy", "White", "US 5-10", "5,6,7,8,9,10", "Recycled Knit", "146.00", 23, "#2563EB", "#E2E8F0", "Crisp winter-inspired colorway on a breathable everyday cushioned chassis.", false),
                shoe("SL-024", "Shadow Ops Boot", "SummitRun", "Trail", "Matte Black / Slate", "Black", "US 9-14", "9,10,11,12,13,14", "Gore-Tex Ripstop", "235.00", 11, "#3B82F6", "#090B10", "All-weather tactical trail boot with gusseted tongue and rock plate.", false),
                shoe("SL-025", "Cobalt Suede Runner", "NorthCraft", "Lifestyle", "Deep Cobalt / Charcoal", "Blue", "US 7-12", "7,8,9,10,11,12", "Suede & Nubuck", "174.00", 17, "#38BDF8", "#1E3A8A", "90s runner aesthetic elevated with premium layered suede panels.", false),
                shoe("SL-026", "ZeroG Dunk High", "ApexKicks", "Basketball", "Navy / Cloud White", "Blue", "US 8-14", "8,9,10,11,12,13,14", "Full-Grain Leather", "190.00", 13, "#60A5FA", "#172554", "High-flying indoor/outdoor court shoe with full-length torsion shank.", false),
                shoe("SL-027", "Circuit Skate Mid", "GridLine", "Skate", "Black / Electric Blue", "Black", "US 6-12", "6,7,8,9,10,11,12", "Canvas & Suede", "105.00", 33, "#2563EB", "#18181B", "Padded collar mid-top engineered for ankle protection on stair sets.", false),
                shoe("SL-028", "Velvet Midnight Loafer", "LuxStride", "Luxury", "Midnight Navy / Black", "Blue", "US 7-12", "7,8,9,10,11,12", "Suede & Nubuck", "260.00", 7, "#3B82F6", "#1E1B4B", "Hybrid dress-sneaker pairing plush navy suede with an ultra-light cup sole.", false),
                shoe("SL-029", "Pulse Wave 4D", "SneakyLab", "Lifestyle", "Obsidian / Aqua Grid", "Black", "US 7-13", "7,8,9,10,11,12,13", "Primeknit Textile", "220.00", 10, "#06B6D4", "#0F172A", "3D-printed lattice midsole calibrated for zoned heel-to-toe energy return.", true),
                shoe("SL-030", "RidgeLine Ultralight", "SummitRun", "Trail", "Slate Blue / Black", "Blue", "US 6-11", "6,7,8,9,10,11", "Ballistic Ripstop", "178.00", 16, "#38BDF8", "#334155", "Fast-packing trail runner with quick-lace toggle and drainage ports.", false),
                shoe("SL-031", "Carbon Stitch Low", "LuxStride", "Luxury", "Pure White / Cobalt", "White", "US 7-12", "7,8,9,10,11,12", "Full-Grain Leather", "240.00", 9, "#2563EB", "#F1F5F9", "Minimalist calfskin sneaker accented with a signature cobalt heel bar.", false),
                shoe("SL-032", "Apex Stride Tempo", "Velocity", "Running", "Royal / Graphite", "Blue", "US 6-11", "6,7,8,9,10,11", "Engineered Mesh", "155.00", 25, "#3B82F6", "#1D4ED8", "Snappy forefoot plate built for 5K intervals and threshold workouts.", false),
                shoe("SL-033", "Blackout Knit Trainer", "IronForm", "Training", "Triple Black", "Black", "US 8-14", "8,9,10,11,12,13,14", "Recycled Knit", "128.00", 37, "#475569", "#090B10", "Stealth all-black gym trainer with breathable upper and firm squat base.", false),
                shoe("SL-034", "Oceanic Drift Slide", "SneakyLab", "Lifestyle", "Deep Blue / Ice", "Blue", "US 5-12", "5,6,7,8,9,10,11,12", "EVA Foam Composite", "65.00", 52, "#38BDF8", "#1E40AF", "One-piece molded recovery slide with contoured arch support.", false),
                shoe("SL-035", "Metro Patent High", "LuxStride", "Luxury", "Black / Chrome Blue", "Black", "US 8-13", "8,9,10,11,12,13", "Patent Leather", "310.00", 4, "#38BDF8", "#09090B", "Statement high-top crafted with mirror-finish leather and metallic eyelets.", false),
                shoe("SL-036", "Kinetic Court Pro", "ApexKicks", "Basketball", "Ice Grey / Sapphire", "Grey", "US 7-13", "7,8,9,10,11,12,13", "Engineered Mesh", "168.00", 21, "#2563EB", "#64748B", "Ultra-breathable guard shoe with razor-sharp lateral outrigger.", false),
                shoe("SL-037", "Grit Vulcan Low", "GridLine", "Skate", "Navy / Gum", "Blue", "US 6-12", "6,7,8,9,10,11,12", "Canvas & Suede", "92.00", 34, "#60A5FA", "#1E3A8A", "Classic navy canvas paired with grippy waffle-pattern gum outsole.", false),
                shoe("SL-038", "CloudBurst Eco", "Velocity", "Running", "Silver / Sky Blue", "Grey", "US 5-10", "5,6,7,8,9,10", "Recycled Knit", "148.00", 28, "#38BDF8", "#94A3B8", "Bio-based EVA foam midsole paired with seamless recycled knit upper.", false),
                shoe("SL-039", "Canyon Shield GTX", "SummitRun", "Trail", "Charcoal / Cyan", "Black", "US 8-13", "8,9,10,11,12,13", "Gore-Tex Ripstop", "205.00", 14, "#06B6D4", "#1F2937", "Mud-shedding outsole geometry with storm-sealed membrane.", false),
                shoe("SL-040", "Heritage 550 Blue", "NorthCraft", "Lifestyle", "White / Royal Blue", "White", "US 6-12", "6,7,8,9,10,11,12", "Full-Grain Leather", "135.00", 30, "#2563EB", "#E2E8F0", "Low-top 80s basketball-turned-streetwear staple with royal overlays.", false),
                shoe("SL-041", "Dynamo Sprint Spike", "SneakyLab", "Running", "Electric Blue / Black", "Blue", "US 6-11", "6,7,8,9,10,11", "Ballistic Ripstop", "172.00", 12, "#38BDF8", "#1D4ED8", "Featherlight track & speed trainer with rigid forefoot Pebax plate.", false),
                shoe("SL-042", "Titan Lift Pro", "IronForm", "Training", "Obsidian / Steel Blue", "Black", "US 9-14", "9,10,11,12,13,14", "Full-Grain Leather", "185.00", 13, "#3B82F6", "#111827", "Elevated 20mm heel weightlifting shoe with dual metatarsal straps.", false),
                shoe("SL-043", "Zephyr Mesh Walker", "NorthCraft", "Lifestyle", "Mist Grey / Cobalt", "Grey", "US 5-10", "5,6,7,8,9,10", "Engineered Mesh", "124.00", 39, "#2563EB", "#94A3B8", "All-day ergonomic commuter shoe with memory-foam collar.", false),
                shoe("SL-044", "Nocturne Suede High", "LuxStride", "Luxury", "Jet Black / Indigo", "Black", "US 7-12", "7,8,9,10,11,12", "Suede & Nubuck", "275.00", 5, "#6366F1", "#090B10", "Velvety black suede upper with subtle indigo leather lining.", false),
                shoe("SL-045", "Vector Skate Slip", "GridLine", "Skate", "Checker Black / Blue", "Black", "US 6-12", "6,7,8,9,10,11,12", "Canvas & Suede", "82.00", 41, "#3B82F6", "#18181B", "Laceless slip-on skate staple with elastic gore and reinforced heel.", false),
                shoe("SL-046", "Cobalt Cloud Runner", "Velocity", "Running", "Cobalt / White", "Blue", "US 7-13", "7,8,9,10,11,12,13", "Primeknit Textile", "164.00", 22, "#38BDF8", "#2563EB", "Plush daily mileage shoe with hollow-pod outsole cushioning.", false),
                shoe("SL-047", "StormBreaker Trail", "SummitRun", "Trail", "Deep Slate / Ice Blue", "Grey", "US 8-14", "8,9,10,11,12,13,14", "Ballistic Ripstop", "192.00", 15, "#38BDF8", "#334155", "High-durability ripstop trail runner built for rocky technical descents.", false),
                shoe("SL-048", "SneakyLink Signature 01", "SneakyLab", "Lifestyle", "Obsidian / Electric Cobalt", "Blue", "US 6-13", "6,7,8,9,10,11,12,13", "Primeknit Textile", "199.00", 18, "#38BDF8", "#1E3A8A", "Flagship SneakyLink edition combining sculpted obsidian foam and cobalt knit.", true)
        );

        shoeRepository.saveAll(catalog);
        log.info("Seeded SneakyLink database with {} footwear models.", catalog.size());
    }

    private static Shoe shoe(String sku, String name, String brand, String category, String colorway,
                             String primaryColor, String sizeRange, String availableSizes, String material,
                             String price, int stock, String accentHex, String bodyHex, String description, boolean featured) {
        String imageUrl = buildSvgSneakerDataUri(brand, sku, accentHex, bodyHex);
        return new Shoe(
                sku, name, brand, category, colorway, primaryColor,
                sizeRange, availableSizes, material,
                new BigDecimal(price), stock, imageUrl, description, featured
        );
    }

    public static String buildSvgSneakerDataUri(String brand, String sku, String accentHex, String bodyHex) {
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 420 260'>"
                + "<defs>"
                + "<linearGradient id='bg' x1='0' y1='0' x2='1' y2='1'>"
                + "<stop offset='0%' stop-color='#0B0F1A'/>"
                + "<stop offset='100%' stop-color='#111B2E'/>"
                + "</linearGradient>"
                + "<radialGradient id='glow' cx='50%' cy='55%' r='45%'>"
                + "<stop offset='0%' stop-color='" + accentHex + "' stop-opacity='0.28'/>"
                + "<stop offset='100%' stop-color='#0B0F1A' stop-opacity='0'/>"
                + "</radialGradient>"
                + "</defs>"
                + "<rect width='420' height='260' fill='url(#bg)'/>"
                + "<ellipse cx='215' cy='150' rx='150' ry='75' fill='url(#glow)'/>"
                + "<ellipse cx='215' cy='212' rx='132' ry='12' fill='#020408' opacity='0.75'/>"
                + "<path d='M78 165 C84 132 115 104 152 102 L190 120 C222 134 252 132 292 146 C324 156 346 168 350 184 L344 196 L74 196 Z' fill='" + bodyHex + "' stroke='" + accentHex + "' stroke-width='2.5'/>"
                + "<path d='M70 186 C140 194 275 194 352 182 L354 198 C275 212 140 212 68 200 Z' fill='#E2E8F0'/>"
                + "<path d='M72 197 C142 208 275 208 352 195 L350 204 C272 216 142 216 74 205 Z' fill='" + accentHex + "'/>"
                + "<path d='M138 148 Q205 162 288 158' fill='none' stroke='" + accentHex + "' stroke-width='5' stroke-linecap='round'/>"
                + "<path d='M178 116 L196 125 M194 124 L212 132 M210 131 L228 138' stroke='#F8FAFC' stroke-width='3' stroke-linecap='round'/>"
                + "<text x='22' y='34' fill='#94A3B8' font-family='sans-serif' font-size='12' font-weight='700' letter-spacing='2'>" + brand.toUpperCase() + "</text>"
                + "<text x='398' y='34' text-anchor='end' fill='" + accentHex + "' font-family='monospace' font-size='11' font-weight='700'>" + sku + "</text>"
                + "</svg>";
        return "data:image/svg+xml;utf8," + URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
