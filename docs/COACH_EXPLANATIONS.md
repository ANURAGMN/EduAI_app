# Coach explanations — for review

This is the source-of-truth for what the coach says at each layer, for every scenario.
Review / edit the text here; once you're happy I'll sync it into `edu-coach.js` and the per-sim hooks.

Each scenario has four layers:
- **Problem** — the full problem quoted in the coach bar (states the actual numbers/target).
- **Answer** — the move the coach reveals (with glow).
- **Why (brief)** — one–two sentences shown in the coach bar after the reveal.
- **Explain (detailed)** — the step-by-step panel opened from the coach card, read aloud by TTS.

> Legend: values in ⟨…⟩ are filled in live from the current round.

---

## MATH — Chapter 1 (Large Numbers)

### 1. Compare (Number System Compare Pitstop · math_1_8)
- **Problem:** Compare ⟨30,000⟩ and ⟨3,00,000⟩. Which is larger — tap "<", "=" or ">"?
- **Answer:** Tap "<" — the first number is smaller.
- **Why:** Rewrite both numbers in the same units — plain digits — first. Then the one with more digits is bigger; if the digit-counts are equal, compare left to right until they differ.
- **Explain:**
  1. The numbers are written in different notations (thousand, lakh, crore, million). First rewrite BOTH as plain digits so they're in the same system: 30 thousand = 30,000 and 3 lakh = 3,00,000.
  2. Count the digits. 30,000 has 5 digits; 3,00,000 has 6. A number with more digits is always larger, so 3 lakh is the bigger number.
  3. Now choose the sign from the FIRST number's point of view: 30,000 is smaller than 3,00,000, so the correct relation is 30,000 "<" 3,00,000.
  4. Why it works: lakh, crore, million and billion are just names for how many zeros a number has. Once both numbers are in plain digits, you can compare them fairly.
  5. Tip: if two numbers have the SAME number of digits, compare them left to right, one digit at a time, and the first place where they differ decides which is bigger.

### 2. Rounding / Nearest place (Rounding Sniper, Nearest Neighbour, Population Radar · math_1_6, 1_9, 1_10, 1_12)
- **Problem:** Round ⟨84,43,675⟩ to the nearest ⟨ten lakh⟩. Which of the options is closest to ⟨84,43,675⟩?
- **Answer:** Rounds to ⟨84,00,000⟩ — tap it.
- **Why:** Look at the part just after the rounding place and compare it to the halfway mark (e.g. 50,000 for a lakh). Halfway or more rounds up; less rounds down.
- **Explain:**
  1. Decide the rounding place. "Nearest lakh" means the answer will be a whole number of lakhs, e.g. 84,00,000 or 85,00,000.
  2. Look only at the part AFTER that place. For 84,43,675 rounded to the nearest lakh, that part is 43,675.
  3. Compare it to half of the place value (half a lakh = 50,000). 43,675 is less than 50,000, so round DOWN → 84,00,000.
  4. If the part had been 50,000 or more, you would round UP → 85,00,000.
  5. Why it works: rounding picks whichever whole lakh the number is closest to, and the halfway mark (50,000) is the exact tipping point.
  6. Tip: rounding to the nearest ten-lakh or crore works the same way — just compare the part after that place to its own halfway value.

### 3. Ratio / "how many times bigger" (Growth Factor Radar, Population Estimate · math_1_9, 1_10)
- **Problem:** About how many times bigger is ⟨1,24,42,373⟩ than ⟨16,84,222⟩? Tap the closest option.
- **Answer:** About ⟨7.4⟩ times — tap "⟨7.4x⟩".
- **Why:** Divide the bigger number by the smaller to find how many times it fits. Round both numbers first so the division is quick to estimate.
- **Explain:**
  1. "How many times bigger" is a division question: bigger ÷ smaller.
  2. Round both numbers to make it easy: about 1.24 crore (124 lakh) and about 17 lakh.
  3. Divide: 124 ÷ 17 ≈ 7.3, so the first number is roughly 7 times the second.
  4. Pick the option closest to your estimate (about 7.4×).
  5. Why it works: division tells you exactly how many copies of the smaller number fit inside the bigger one.
  6. Tip: rounding first keeps the division simple, and the estimate still lands very close to the real answer.

### 4. Indian vs International commas (Comma System Relay · math_1_2_new; Indian vs International · math_1_2)
- **Problem:** Number is ⟨27582914⟩. Pick the card where BOTH the Indian and International groupings are correct.
- **Answer:** Indian ⟨2,75,82,914⟩, International ⟨27,582,914⟩ — pick that card.
- **Why:** Indian and International systems group digits differently, so a card is correct only if BOTH lines follow their own rule.
- **Explain:**
  1. Take the number ⟨27582914⟩.
  2. Indian system: starting from the right, place the first comma after 3 digits, then after every 2 digits → 2,75,82,914 (…crore, lakh, thousand).
  3. International system: starting from the right, place a comma after every 3 digits → 27,582,914 (…million, thousand).
  4. The correct card must show BOTH: Indian 2,75,82,914 and International 27,582,914. If either line is grouped wrongly, that card is wrong.
  5. Why it works: the two systems name groups differently (lakh/crore vs thousand/million), so the comma spacing genuinely differs — 2-digit groups after the first three in Indian, 3-digit groups throughout in International.

### 5. Multiplication / number pattern (Multiplication Pattern Pulse · math_1_5_new)
- **Problem:** The sequence so far is ⟨3, 33, 333⟩, … What number comes next?
- **Answer:** Next is ⟨3,333⟩ (⟨4⟩ digits).
- **Why:** Find the rule that turns one term into the next (×10, add a digit, or square). Confirm it on two pairs, then apply it once more for the answer.
- **Explain:**
  1. Look at how the first term becomes the second: 3 → 33 (multiply by 10 and add 3, i.e. put another 3 on the end).
  2. Check the SAME rule works for the next pair: 33 → 333. It does, so the rule is confirmed.
  3. Apply the rule once more: 333 → 3,333. That's the next term.
  4. Count its digits (3,333 has 4) and enter that if the sim asks.
  5. Why it works: a pattern means one fixed rule links every pair of terms, so once you find the rule you can extend the sequence as far as you like.

### 6. Place-value builder (Creative Chitti Calculator · math_1_1)
- **Problem:** Target: ⟨40,629⟩. You've built ⟨40,000⟩, so ⟨629⟩ to go. Which place-value button gets you closest to ⟨40,629⟩ without going over?
- **Answer:** Tap ⟨+100⟩ — ⟨629⟩ to go (⟨40,000⟩ of ⟨40,629⟩).
- **Why:** Fill the highest place value first, then work downward. Always add the biggest button that still fits without overshooting — that reaches the target in the fewest taps.
- **Explain:**
  1. Read the target, e.g. 5,072. Build it place by place, biggest first.
  2. Add +1000 five times → 5,000 (five taps).
  3. Move to the next place: the tens digit is 7, so add +10 seven times → 5,070.
  4. Finish with the ones: the ones digit is 2, so add +1 twice → 5,072. Done!
  5. Why it works: each button matches one place value, and the digit in each place tells you exactly how many taps you need there. Adding the biggest button that still fits never wastes a tap or overshoots.
  6. Tip: the fewest possible taps equals the sum of the digits (5 + 0 + 7 + 2 = 14 taps for 5,072).

### 7. Restricted calculator (Restricted Calculator Rush · math_1_11)
- **Problem:** Target: ⟨9,000⟩. The only button adds +⟨1000⟩ each tap, and you are at ⟨3,000⟩. How many taps will reach ⟨9,000⟩?
- **Answer:** Tap +⟨1000⟩ — ⟨6⟩ more (⟨3,000⟩ of ⟨9,000⟩).
- **Why:** Each tap adds the same fixed amount. Divide the remaining gap by the step size to know exactly how many taps you need.
- **Explain:**
  1. Find the gap between where you are and the target: 9,000 − 3,000 = 6,000.
  2. Divide by the step size: 6,000 ÷ 1000 = 6 taps.
  3. Tap +1000 six times, then press Check.
  4. Why it works: pressing the same button again and again is repeated addition, which is multiplication — so the number of taps is just the gap divided by the step.

### 8. Maximize / minimize (Card Sum Difference Duel · math_1_1_new)
- **Problem:** Make A + B as large as possible — A has ⟨5⟩ digits and B has ⟨4⟩. Which digit goes in the highest empty place next?
  *(Also runs as "Minimize |A − B|" and "Maximize |A − B|".)*
- **Answer:** Place ⟨9⟩ into ⟨A⟩ (the next highest place).
- **Why:** A digit is worth more in a higher place, so put the biggest digits in the highest places. Share them between the two numbers, alternating from the largest.
- **Explain:**
  - **Maximize A + B:** sort digits high→low and fill the highest place values first, alternating A and B (9→A, 8→B, 7→A, …). Example with 9…1: A = 97,531, B = 8,642. A digit in the ten-thousands place is worth 10,000×, so the biggest digits must sit in the highest places.
  - **Minimize |A − B|:** make the two numbers as close as possible — give them near-equal leading digits, then split the rest so one number's remaining digits are as small as the other's are large.
  - **Maximize |A − B|:** make one number as big as possible and the other as small as possible — biggest digits into A's high places, smallest digits into B.

### 9. Digit permutation vault (Digit Permutation Vault · math_1_3_new)
- **Problem:** Using the given digits, make the ⟨largest⟩ number that fits the rule.
- **Answer:** Arrange digits ⟨9 down⟩; ⟨it must end in an even digit⟩.
- **Why:** Place value decides size: sort digits high-to-low for the largest number (or low-to-high for the smallest), then fix the last digit for the rule.
- **Explain:**
  1. Read the rule (even, odd, multiple of 5). Example: make the LARGEST number from 4, 7, 1, 9 that is even.
  2. The rule pins the LAST digit: "even" means it ends in 0/2/4/6/8. Here only 4 qualifies, so 4 goes at the end.
  3. Arrange the remaining digits (9, 7, 1) biggest-first in the higher places → 9, 7, 1.
  4. Put it together: 9714.
  5. Why it works: the leftmost digits control the size the most, so big digits go left; the rule only constrains the last digit, so satisfy it first, then maximise the rest.
  6. Tip: for the SMALLEST number, sort low-to-high — but never start with 0.

### 10. Target dash (Number Cards Target Dash · math_1_7)
- **Problem:** Reach ⟨the target⟩ by combining the number cards with + − × ÷.
- **Answer:** Start with the biggest cards and × or −, then nudge with the small cards.
- **Why:** Big cards with × cover most of the distance; small cards with + or − fine-tune the rest.
- **Explain:**
  1. Look at how far the target is and which cards are biggest.
  2. Use the biggest cards first with × (to jump far) or − (to cut down) to get near the target.
  3. Then use the small cards with + or − to close the last gap exactly.
  4. Why it works: multiplication changes the value in big steps and addition/subtraction in small ones, so combine both to land precisely.

### 11. Scale journey (Scale Journey Builder · math_1_13)
- **Problem:** Target distance ⟨3,84,400 km⟩ in ⟨3,650 days⟩. Which daily speed reaches it?
- **Answer:** ⟨100⟩ km/day works: 100 × 3,650 ≈ 3,65,000 km.
- **Why:** distance ≈ speed × days, so try each speed × the days and see which lands near the target.
- **Explain:**
  1. The distance you cover = daily speed × number of days.
  2. Multiply each speed option by the days: 100 × 3,650 = 3,65,000 km, close to 3,84,400.
  3. Pick the speed whose total is closest to the target distance.
  4. Why it works: travelling the same distance every day means total distance is just speed multiplied by the number of days.

### 12. Sense of scale (Large Number Visualizer · math_1_3 — explorer)
- **Problem (guiding, no single answer):** e.g. "Drag Speed and Duration — how fast must you go to reach the Moon (384,400 km)?"
- **Explain:** This is an explorer. Encourage the learner to change the sliders and watch how quickly huge numbers build up (e.g. 1 lakh sheets of paper at 5 g each already weigh 500 kg). The takeaway: multiplication makes numbers grow astonishingly fast.

---

## MATH — Chapter 2 (Arithmetic Expressions)

### 13. Terms & value of an expression (Terms & Expression Evaluator · math_2_1)
- **Problem:** ⟨28 − 7 + 8⟩ → how many terms, and what is its value?
- **Answer:** ⟨3⟩ terms, value = ⟨29⟩.
- **Why:** Split the expression at + and − signs to count terms; do × and ÷ first, then add and subtract left to right.
- **Explain:**
  1. Terms are the parts separated by + and − signs. 28 − 7 + 8 has three terms: 28, −7 and +8.
  2. To find the value, do any × or ÷ first (there are none here), then add and subtract left to right: 28 − 7 = 21, 21 + 8 = 29.
  3. Why it works: + and − mark where one term ends and the next begins, while × and ÷ bind numbers together into a single term, so they must be done first.

### 14. Removing brackets / sign rules (Brackets & Sign Rules · math_2_2)
- **Problem:** Remove the brackets from ⟨…⟩ — which expression is equal?
- **Answer:** The equal expression is "⟨…⟩".
- **Why:** A + before a bracket keeps the signs inside; a − before a bracket flips every sign inside.
- **Explain:**
  1. Look at the sign just before the bracket.
  2. If it's +, drop the bracket and keep every sign inside unchanged.
  3. If it's −, drop the bracket and FLIP every sign inside (+ becomes −, − becomes +).
  4. Example: 10 − (3 − 2) = 10 − 3 + 2 = 9.
  5. Why it works: subtracting a group means subtracting each part of it, which reverses each sign.

### 15. Expression comparison — reason, don't calculate (Expression Comparison · math_2_4)
- **Problem:** Compare ⟨…⟩ and ⟨…⟩ — which sign fits, <, = or >?
- **Answer:** It's "⟨…⟩".
- **Why:** Look for structure (same terms, an extra bit added/removed) instead of fully calculating both sides.
- **Explain:**
  1. Don't rush to calculate — compare the structure of the two sides.
  2. If both sides share the same terms and one has something extra added, that side is bigger; if something is subtracted, it's smaller.
  3. Example: 25 × 8 vs 25 × 8 + 1 — the right side is clearly bigger by 1, so <.
  4. Why it works: reasoning about what's added or removed avoids long arithmetic and shows the relationship directly.

### 16. Associativity (Associative Chain Reactor · math_2_1_new)
- **Problem:** Which regrouping of ⟨(a) + (b) + (c)⟩ keeps the same total?
- **Answer:** Both groupings keep the same total — tap either to confirm.
- **Why:** Addition is associative: regrouping which numbers you add first never changes the total.
- **Explain:**
  1. Associativity means (a + b) + c = a + (b + c) — the total is the same no matter how you group the additions.
  2. Check it with numbers: (12 + (−7)) + 5 = 10 and 12 + ((−7) + 5) = 10. Same answer.
  3. Why it works: addition just combines amounts, and the order/grouping of combining doesn't change how much you have in total.

### 17. Distributive shortcut (Distributive Mental Math Burst · math_2_5_new)
- **Problem:** What's a smart way to compute ⟨97 × 25⟩ using round numbers?
- **Answer:** Use: ⟨(100 − 3) × 25 = 2500 − 75 = 2425⟩.
- **Why:** Break a number into a round number ± a little, then distribute the multiplication over each part.
- **Explain:**
  1. Rewrite the awkward number as a round number plus or minus a small amount: 97 = 100 − 3.
  2. Distribute the multiplication over both parts: (100 − 3) × 25 = 100 × 25 − 3 × 25.
  3. Compute the easy pieces: 2500 − 75 = 2425.
  4. Why it works: the distributive law says a × (b + c) = a×b + a×c, so splitting into round numbers turns a hard multiplication into two easy ones.

---

## SCIENCE — Chapter (Acids, Bases & Salts)

### 18. Acid / base indicators (Litmus, Red Rose, Turmeric, Olfactory · science_2_2…2_6)
- **Problem (walkthrough):** Test each substance in turn: "Next, test ⟨Vinegar⟩ — tap it," then "⟨Vinegar⟩ is ⟨an acid⟩ — tap ⟨Dip Papers⟩ to test it."
- **Answer:** Guides through every substance, one at a time, until all are tested.
- **Why:** An indicator changes colour (or smell) differently for acids, bases and neutral substances, so testing them all reveals the pattern.
- **Explain:**
  1. An indicator is something that changes appearance depending on whether a substance is an acid, a base or neutral.
  2. Litmus: acids turn blue litmus RED; bases turn red litmus BLUE; neutral substances don't change it.
  3. Test each substance (lemon, vinegar, soap, baking soda, water, …) and note the result — acids and bases fall into clear groups.
  4. Why it works: acids and bases have opposite chemical behaviour, and indicators react to that difference with a visible signal.

### 19. Conductors & insulators (Material Testing · science_3_10)
- **Problem (walkthrough):** Tap ⟨Metal Spoon⟩ — it is ⟨a conductor⟩ (the bulb glows). Then move to the next material.
- **Answer:** Guides through every material, saying conductor or insulator.
- **Why:** A material is a conductor if it lets current flow and lights the bulb; an insulator blocks the current.
- **Explain:**
  1. Put each material into the circuit and watch the bulb.
  2. If the bulb glows, current flows → the material is a CONDUCTOR (metals: spoon, key, coin, foil).
  3. If the bulb stays off, current is blocked → the material is an INSULATOR (plastic, rubber, wood, glass).
  4. Why it works: metals have free electrons that can move and carry current; non-metals hold their electrons tightly, so current can't pass.

---

## MATH — Chapter 3 (Decimals)

Per-simulation coach text, explanations, glow behaviour and unit tests for all 15 Ch 3 English sims
live in the dedicated spec — that file is the single source of truth for Ch 3, so entries are not
duplicated here:

- **`docs/COACH_SPEC_math_ch3.md`**

(Chapter 2's full catalogue is likewise in `docs/COACH_SPEC_math_ch2.md`.)

---

## Notes for review
- Tell me which explanations to lengthen, shorten, or reword (tone, vocabulary level for class 7).
- Any scenario missing above (e.g. specific ch2 sims like Operator Ladder, Sign Flip, Term Sort, Distributive Explorers) — I'll add its entry as I build that hook.
