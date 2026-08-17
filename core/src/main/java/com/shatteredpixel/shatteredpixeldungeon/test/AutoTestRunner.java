/*
 * Eternity Pixel Dungeon
 * Auto-Test Runner Framework
 */

package com.shatteredpixel.shatteredpixeldungeon.test;

import java.util.ArrayList;
import java.util.List;

public class AutoTestRunner {

	public static int runAllTests() {
		System.out.println("===============================================================");
		System.out.println(" 🤖 ETERNITY PIXEL DUNGEON — AUTO-TEST RUNNER SUITE");
		System.out.println("===============================================================");

		HeadlessEnvironment.init();

		List<TestResult> results = new ArrayList<>();
		long totalStart = System.currentTimeMillis();

		// Run Test Modules
		System.out.println("\n[1/5] Ejecutando: Dungeon Generation & Reachability Test...");
		results.add(DungeonGenTest.run());

		System.out.println("[2/5] Ejecutando: Hero Mechanics & Classes Test...");
		results.add(HeroMechanicsTest.run());

		System.out.println("[3/5] Ejecutando: Pets & Companions System Test...");
		results.add(PetSystemTest.run());

		System.out.println("[4/6] Ejecutando: Item Rarity & Generation Test...");
		results.add(ItemRarityTest.run());

		System.out.println("[5/6] Ejecutando: Platform Achievements & Badges Sync Test...");
		results.add(PlatformAchievementsTest.run());

		System.out.println("[6/6] Ejecutando: Autonomous Bot AI Simulation (50 turns)...");
		results.add(AutonomousBotSim.run(50));

		long totalDuration = System.currentTimeMillis() - totalStart;

		// Summary Report
		int totalRun = 0;
		int totalPassed = 0;
		int totalFailed = 0;
		boolean allPassed = true;
		StringBuilder report = new StringBuilder();
		report.append("===============================================================\n");
		report.append(" 🤖 ETERNITY PIXEL DUNGEON — REPORTE DE AUTO-TEST RUNNER\n");
		report.append(" Fecha: ").append(new java.util.Date()).append("\n");
		report.append("===============================================================\n\n");

		for (TestResult tr : results) {
			totalRun += tr.testsRun;
			totalPassed += tr.testsPassed;
			totalFailed += tr.testsFailed;
			if (!tr.passed) allPassed = false;

			String statusBadge = tr.passed ? "[ PASS ]" : "[ FAIL ]";
			String header = String.format("%s %s (%d ms, %d/%d tests passed)\n",
					statusBadge, tr.suiteName, tr.durationMs, tr.testsPassed, tr.testsRun);
			System.out.println("\n" + header.trim());
			report.append(header);

			for (String line : tr.details) {
				System.out.println("  " + line);
				report.append("  ").append(line).append("\n");
			}
			report.append("\n");
		}

		String summary = String.format(
				"===============================================================\n" +
				" 🏁 RESUMEN GENERAL\n" +
				"===============================================================\n" +
				"  Total Pruebas:  %d\n" +
				"  Exitosas:       %d\n" +
				"  Fallidas:       %d\n" +
				"  Tiempo Total:   %.2f s\n" +
				"===============================================================\n" +
				(allPassed ? " 🎉 RESULTADO FINAL: ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!\n"
						   : " ❌ RESULTADO FINAL: SE ENCONTRARON FALLOS EN LA SUITE DE PRUEBAS.\n"),
				totalRun, totalPassed, totalFailed, totalDuration / 1000.0
		);

		System.out.println("\n" + summary);
		report.append(summary);

		try {
			java.io.File reportDir = new java.io.File("desktop/build/reports");
			if (!reportDir.exists()) reportDir.mkdirs();
			java.io.FileWriter writer = new java.io.FileWriter("desktop/build/reports/autoTest-report.txt");
			writer.write(report.toString());
			writer.close();
			System.out.println(" 📄 Reporte guardado en: desktop/build/reports/autoTest-report.txt\n");
		} catch (Exception ignored) {}

		return allPassed ? 0 : 1;
	}
}
