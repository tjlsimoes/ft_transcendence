import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-about',
  imports: [RouterLink],
  templateUrl: './about.html',
  styleUrl: './about.css',
})
export class About {
  protected readonly highlights = [
    { value: '1v1', label: 'live matches' },
    { value: 'LP', label: 'ranked climb' },
    { value: 'FAST', label: 'short pressure rounds' },
  ];

  protected readonly reasons = [
    {
      title: 'Every match feels personal',
      description:
        'You and your opponent get the same problem, the same timer, and one chance to prove who handles pressure better.',
    },
    {
      title: 'You improve while competing',
      description:
        'The best part is not only winning. It is feeling your decisions get faster, cleaner, and sharper match after match.',
    },
    {
      title: 'The ladder gives every game meaning',
      description:
        'Bronze to Legend gives you a visible path. Each win moves you forward, and each loss gives you a target to chase again.',
    },
  ];

  protected readonly playerGuide = [
    {
      title: 'Queue up',
      description: 'Enter the arena and get matched with someone close to your level.',
    },
    {
      title: 'Solve under pressure',
      description:
        'Read fast, think clearly, and submit before the clock turns your hands cold.',
    },
    {
      title: 'Climb the ladder',
      description:
        'Win LP, move through the leagues, and build a profile that shows how far you have come.',
    },
  ];

  protected readonly leagues = [
    {
      name: 'Bronze',
      lp: '0 - 999 LP',
      tone: 'league-bronze',
      mark: 'B',
      focus: 'Learn the pace and start building confidence.',
    },
    {
      name: 'Silver',
      lp: '1000 - 1999 LP',
      tone: 'league-silver',
      mark: 'S',
      focus: 'Cleaner reads, faster reactions, stronger rivals.',
    },
    {
      name: 'Gold',
      lp: '2000 - 2999 LP',
      tone: 'league-gold',
      mark: 'G',
      focus: 'Pressure rises and every small mistake starts to matter.',
    },
    {
      name: 'Master',
      lp: '3000 - 3999 LP',
      tone: 'league-master',
      mark: 'M',
      focus: 'High-skill duels where speed and control meet.',
    },
    {
      name: 'Legend',
      lp: 'Top 1%',
      tone: 'league-legend',
      mark: 'L',
      focus: 'For players who make the arena feel small.',
    },
  ];
}
