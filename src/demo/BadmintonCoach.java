package demo;

import org.springframework.beans.factory.DisposableBean;

public class BadmintonCoach implements Coach, DisposableBean {

	private FortuneService fortuneService;

	public BadmintonCoach() {
		
	}
	
	public BadmintonCoach(FortuneService fortuneService) {
		this.fortuneService = fortuneService;
	}

	@Override
	public String getDailyWorkout() {
		return "Spend 2 hours on Badminton";
	}

	@Override
	public String getDailyFortune() {
		return "Just Do It: " + fortuneService.getFortune();
	}

	// add an init method
	public void doMyStartupStuff() {
		System.out.println("BadmintonCoach: inside method doMyStartupStuff");
	}
	
	// add a destroy method
	@Override
	public void destroy() throws Exception {
		System.out.println("BadmintonCoach: inside method doMyCleanupStuff");		
	}
}










